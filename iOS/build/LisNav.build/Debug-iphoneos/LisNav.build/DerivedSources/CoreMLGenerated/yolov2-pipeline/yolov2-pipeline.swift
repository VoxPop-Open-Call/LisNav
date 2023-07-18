//
// yolov2_pipeline.swift
//
// This file was automatically generated and should not be edited.
//

import CoreML


/// Model Prediction Input Type
@available(macOS 10.14, iOS 12.0, tvOS 12.0, watchOS 5.0, *)
class yolov2_pipelineInput : MLFeatureProvider {

    /// input.1 as color (kCVPixelFormatType_32BGRA) image buffer, 416 pixels wide by 416 pixels high
    var input_1: CVPixelBuffer

    /// iouThreshold as double value
    var iouThreshold: Double

    /// confidenceThreshold as double value
    var confidenceThreshold: Double

    var featureNames: Set<String> {
        get {
            return ["input.1", "iouThreshold", "confidenceThreshold"]
        }
    }
    
    func featureValue(for featureName: String) -> MLFeatureValue? {
        if (featureName == "input.1") {
            return MLFeatureValue(pixelBuffer: input_1)
        }
        if (featureName == "iouThreshold") {
            return MLFeatureValue(double: iouThreshold)
        }
        if (featureName == "confidenceThreshold") {
            return MLFeatureValue(double: confidenceThreshold)
        }
        return nil
    }
    
    init(input_1: CVPixelBuffer, iouThreshold: Double, confidenceThreshold: Double) {
        self.input_1 = input_1
        self.iouThreshold = iouThreshold
        self.confidenceThreshold = confidenceThreshold
    }

    @available(macOS 10.15, iOS 13.0, tvOS 13.0, watchOS 6.0, *)
    convenience init(input_1With input_1: CGImage, iouThreshold: Double, confidenceThreshold: Double) throws {
        self.init(input_1: try MLFeatureValue(cgImage: input_1, pixelsWide: 416, pixelsHigh: 416, pixelFormatType: kCVPixelFormatType_32ARGB, options: nil).imageBufferValue!, iouThreshold: iouThreshold, confidenceThreshold: confidenceThreshold)
    }

    @available(macOS 10.15, iOS 13.0, tvOS 13.0, watchOS 6.0, *)
    convenience init(input_1At input_1: URL, iouThreshold: Double, confidenceThreshold: Double) throws {
        self.init(input_1: try MLFeatureValue(imageAt: input_1, pixelsWide: 416, pixelsHigh: 416, pixelFormatType: kCVPixelFormatType_32ARGB, options: nil).imageBufferValue!, iouThreshold: iouThreshold, confidenceThreshold: confidenceThreshold)
    }

    @available(macOS 10.15, iOS 13.0, tvOS 13.0, watchOS 6.0, *)
    func setInput_1(with input_1: CGImage) throws  {
        self.input_1 = try MLFeatureValue(cgImage: input_1, pixelsWide: 416, pixelsHigh: 416, pixelFormatType: kCVPixelFormatType_32ARGB, options: nil).imageBufferValue!
    }

    @available(macOS 10.15, iOS 13.0, tvOS 13.0, watchOS 6.0, *)
    func setInput_1(with input_1: URL) throws  {
        self.input_1 = try MLFeatureValue(imageAt: input_1, pixelsWide: 416, pixelsHigh: 416, pixelFormatType: kCVPixelFormatType_32ARGB, options: nil).imageBufferValue!
    }

}


/// Model Prediction Output Type
@available(macOS 10.14, iOS 12.0, tvOS 12.0, watchOS 5.0, *)
class yolov2_pipelineOutput : MLFeatureProvider {

    /// Source provided by CoreML
    private let provider : MLFeatureProvider

    /// scores as multidimensional array of doubles
    var scores: MLMultiArray {
        return self.provider.featureValue(for: "scores")!.multiArrayValue!
    }

    /// scores as multidimensional array of doubles
    @available(macOS 12.0, iOS 15.0, tvOS 15.0, watchOS 8.0, *)
    var scoresShapedArray: MLShapedArray<Double> {
        return MLShapedArray<Double>(self.scores)
    }

    /// boxes as multidimensional array of doubles
    var boxes: MLMultiArray {
        return self.provider.featureValue(for: "boxes")!.multiArrayValue!
    }

    /// boxes as multidimensional array of doubles
    @available(macOS 12.0, iOS 15.0, tvOS 15.0, watchOS 8.0, *)
    var boxesShapedArray: MLShapedArray<Double> {
        return MLShapedArray<Double>(self.boxes)
    }

    var featureNames: Set<String> {
        return self.provider.featureNames
    }
    
    func featureValue(for featureName: String) -> MLFeatureValue? {
        return self.provider.featureValue(for: featureName)
    }

    init(scores: MLMultiArray, boxes: MLMultiArray) {
        self.provider = try! MLDictionaryFeatureProvider(dictionary: ["scores" : MLFeatureValue(multiArray: scores), "boxes" : MLFeatureValue(multiArray: boxes)])
    }

    init(features: MLFeatureProvider) {
        self.provider = features
    }
}


/// Class for model loading and prediction
@available(macOS 10.14, iOS 12.0, tvOS 12.0, watchOS 5.0, *)
class yolov2_pipeline {
    let model: MLModel

    /// URL of model assuming it was installed in the same bundle as this class
    class var urlOfModelInThisBundle : URL {
        let bundle = Bundle(for: self)
        return bundle.url(forResource: "yolov2-pipeline", withExtension:"mlmodelc")!
    }

    /**
        Construct yolov2_pipeline instance with an existing MLModel object.

        Usually the application does not use this initializer unless it makes a subclass of yolov2_pipeline.
        Such application may want to use `MLModel(contentsOfURL:configuration:)` and `yolov2_pipeline.urlOfModelInThisBundle` to create a MLModel object to pass-in.

        - parameters:
          - model: MLModel object
    */
    init(model: MLModel) {
        self.model = model
    }

    /**
        Construct yolov2_pipeline instance by automatically loading the model from the app's bundle.
    */
    @available(*, deprecated, message: "Use init(configuration:) instead and handle errors appropriately.")
    convenience init() {
        try! self.init(contentsOf: type(of:self).urlOfModelInThisBundle)
    }

    /**
        Construct a model with configuration

        - parameters:
           - configuration: the desired model configuration

        - throws: an NSError object that describes the problem
    */
    convenience init(configuration: MLModelConfiguration) throws {
        try self.init(contentsOf: type(of:self).urlOfModelInThisBundle, configuration: configuration)
    }

    /**
        Construct yolov2_pipeline instance with explicit path to mlmodelc file
        - parameters:
           - modelURL: the file url of the model

        - throws: an NSError object that describes the problem
    */
    convenience init(contentsOf modelURL: URL) throws {
        try self.init(model: MLModel(contentsOf: modelURL))
    }

    /**
        Construct a model with URL of the .mlmodelc directory and configuration

        - parameters:
           - modelURL: the file url of the model
           - configuration: the desired model configuration

        - throws: an NSError object that describes the problem
    */
    convenience init(contentsOf modelURL: URL, configuration: MLModelConfiguration) throws {
        try self.init(model: MLModel(contentsOf: modelURL, configuration: configuration))
    }

    /**
        Construct yolov2_pipeline instance asynchronously with optional configuration.

        Model loading may take time when the model content is not immediately available (e.g. encrypted model). Use this factory method especially when the caller is on the main thread.

        - parameters:
          - configuration: the desired model configuration
          - handler: the completion handler to be called when the model loading completes successfully or unsuccessfully
    */
    @available(macOS 11.0, iOS 14.0, tvOS 14.0, watchOS 7.0, *)
    class func load(configuration: MLModelConfiguration = MLModelConfiguration(), completionHandler handler: @escaping (Swift.Result<yolov2_pipeline, Error>) -> Void) {
        return self.load(contentsOf: self.urlOfModelInThisBundle, configuration: configuration, completionHandler: handler)
    }

    /**
        Construct yolov2_pipeline instance asynchronously with optional configuration.

        Model loading may take time when the model content is not immediately available (e.g. encrypted model). Use this factory method especially when the caller is on the main thread.

        - parameters:
          - configuration: the desired model configuration
    */
    @available(macOS 12.0, iOS 15.0, tvOS 15.0, watchOS 8.0, *)
    class func load(configuration: MLModelConfiguration = MLModelConfiguration()) async throws -> yolov2_pipeline {
        return try await self.load(contentsOf: self.urlOfModelInThisBundle, configuration: configuration)
    }

    /**
        Construct yolov2_pipeline instance asynchronously with URL of the .mlmodelc directory with optional configuration.

        Model loading may take time when the model content is not immediately available (e.g. encrypted model). Use this factory method especially when the caller is on the main thread.

        - parameters:
          - modelURL: the URL to the model
          - configuration: the desired model configuration
          - handler: the completion handler to be called when the model loading completes successfully or unsuccessfully
    */
    @available(macOS 11.0, iOS 14.0, tvOS 14.0, watchOS 7.0, *)
    class func load(contentsOf modelURL: URL, configuration: MLModelConfiguration = MLModelConfiguration(), completionHandler handler: @escaping (Swift.Result<yolov2_pipeline, Error>) -> Void) {
        MLModel.load(contentsOf: modelURL, configuration: configuration) { result in
            switch result {
            case .failure(let error):
                handler(.failure(error))
            case .success(let model):
                handler(.success(yolov2_pipeline(model: model)))
            }
        }
    }

    /**
        Construct yolov2_pipeline instance asynchronously with URL of the .mlmodelc directory with optional configuration.

        Model loading may take time when the model content is not immediately available (e.g. encrypted model). Use this factory method especially when the caller is on the main thread.

        - parameters:
          - modelURL: the URL to the model
          - configuration: the desired model configuration
    */
    @available(macOS 12.0, iOS 15.0, tvOS 15.0, watchOS 8.0, *)
    class func load(contentsOf modelURL: URL, configuration: MLModelConfiguration = MLModelConfiguration()) async throws -> yolov2_pipeline {
        let model = try await MLModel.load(contentsOf: modelURL, configuration: configuration)
        return yolov2_pipeline(model: model)
    }

    /**
        Make a prediction using the structured interface

        - parameters:
           - input: the input to the prediction as yolov2_pipelineInput

        - throws: an NSError object that describes the problem

        - returns: the result of the prediction as yolov2_pipelineOutput
    */
    func prediction(input: yolov2_pipelineInput) throws -> yolov2_pipelineOutput {
        return try self.prediction(input: input, options: MLPredictionOptions())
    }

    /**
        Make a prediction using the structured interface

        - parameters:
           - input: the input to the prediction as yolov2_pipelineInput
           - options: prediction options 

        - throws: an NSError object that describes the problem

        - returns: the result of the prediction as yolov2_pipelineOutput
    */
    func prediction(input: yolov2_pipelineInput, options: MLPredictionOptions) throws -> yolov2_pipelineOutput {
        let outFeatures = try model.prediction(from: input, options:options)
        return yolov2_pipelineOutput(features: outFeatures)
    }

    /**
        Make a prediction using the convenience interface

        - parameters:
            - input_1 as color (kCVPixelFormatType_32BGRA) image buffer, 416 pixels wide by 416 pixels high
            - iouThreshold as double value
            - confidenceThreshold as double value

        - throws: an NSError object that describes the problem

        - returns: the result of the prediction as yolov2_pipelineOutput
    */
    func prediction(input_1: CVPixelBuffer, iouThreshold: Double, confidenceThreshold: Double) throws -> yolov2_pipelineOutput {
        let input_ = yolov2_pipelineInput(input_1: input_1, iouThreshold: iouThreshold, confidenceThreshold: confidenceThreshold)
        return try self.prediction(input: input_)
    }

    /**
        Make a batch prediction using the structured interface

        - parameters:
           - inputs: the inputs to the prediction as [yolov2_pipelineInput]
           - options: prediction options 

        - throws: an NSError object that describes the problem

        - returns: the result of the prediction as [yolov2_pipelineOutput]
    */
    func predictions(inputs: [yolov2_pipelineInput], options: MLPredictionOptions = MLPredictionOptions()) throws -> [yolov2_pipelineOutput] {
        let batchIn = MLArrayBatchProvider(array: inputs)
        let batchOut = try model.predictions(from: batchIn, options: options)
        var results : [yolov2_pipelineOutput] = []
        results.reserveCapacity(inputs.count)
        for i in 0..<batchOut.count {
            let outProvider = batchOut.features(at: i)
            let result =  yolov2_pipelineOutput(features: outProvider)
            results.append(result)
        }
        return results
    }
}
