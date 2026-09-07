package com.github.laxika.magicalvibes.model.effect;

/**
 * Entry-choice marker for an Equipment that enters attached to a creature its controller controls
 * when a legal choice exists. The choice is made before the permanent's enter-the-battlefield
 * abilities are collected.
 */
public record ChooseEquipmentAttachmentOnEnterEffect() implements CardEffect {
}
