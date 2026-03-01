package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.List;
import java.util.UUID;

public record Emblem(UUID controllerId, List<CardEffect> staticEffects, Card sourceCard) {
}
