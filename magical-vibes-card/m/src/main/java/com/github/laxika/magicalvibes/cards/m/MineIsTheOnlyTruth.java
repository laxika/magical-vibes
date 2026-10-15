package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerDrewAtLeastCardsLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "DSC", collectorNumber = "340")
public class MineIsTheOnlyTruth extends Card {

    public MineIsTheOnlyTruth() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new SpellCastTriggerEffect(null, List.of(new DrawCardEffect())));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(new ControllerDrewAtLeastCardsLastTurn(1), new SacrificeSelfEffect()));
    }
}
