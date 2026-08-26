package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.PowerToughnessForm;

import java.util.List;

/** Replacement-style choice that stamps one of a permanent's base power/toughness forms. */
public interface PowerToughnessFormChoiceEffect extends ReplacementEffect {

    List<PowerToughnessForm> forms();
}
