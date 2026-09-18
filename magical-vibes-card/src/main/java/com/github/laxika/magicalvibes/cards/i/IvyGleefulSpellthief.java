package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsOnlySingleCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "201")
public class IvyGleefulSpellthief extends Card {

    public IvyGleefulSpellthief() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(
                        null,
                        List.of(CopyTriggeringSpellEffect.targetingSource()),
                        null,
                        null,
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryTargetsOnlySingleCreaturePredicate(),
                                new StackEntryNotPredicate(new StackEntryTargetsSourcePredicate())
                        )),
                        false,
                        false,
                        null,
                        0,
                        0,
                        true,
                        false
                ),
                "Copy that spell?"));
    }
}
