package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.EnumSet;
import java.util.List;

/**
 * Target creature you control gains protection from each color until end of turn.
 *
 * <p>Overload {4}{C} changes the target version to protect each creature you control instead.
 */
@CardRegistration(set = "M3C", collectorNumber = "33")
public class EldritchImmunity extends Card {

    public EldritchImmunity() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{4}{C}"))));

        ProtectionFromColorsEffect protection = new ProtectionFromColorsEffect(
                EnumSet.allOf(CardColor.class));
        addSpellEffect(new ConditionalReplacementEffect(
                new Overloaded(),
                new GrantEffectToTargetUntilEndOfTurnEffect(StaticAbility.of(protection)),
                new GrantEffectToTargetUntilEndOfTurnEffect(StaticAbility.of(protection), Subjects.creaturesYouControl())));
        target(TargetFilters.creatureYouControl());
    }
}
