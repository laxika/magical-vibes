package com.github.laxika.magicalvibes.networking.message;
import com.github.laxika.magicalvibes.networking.model.MessageType;
public record ValidateDeckResponse(MessageType type, com.github.laxika.magicalvibes.model.DeckValidation validation) {}
