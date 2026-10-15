package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "127")
public class MythosOfVadrok extends Card {

    public MythosOfVadrok() {
        addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongTargetCreaturesAndPlaneswalkers(5));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new AllConditions(List.of(
                        new ColorSpentToCast(ManaColor.WHITE),
                        new ColorSpentToCast(ManaColor.BLUE))),
                new LockTargetPermanentEffect(true, true, true,
                        EffectDuration.UNTIL_YOUR_NEXT_TURN,
                        TargetPredicates.creatureOrPlaneswalker())));
    }
}
