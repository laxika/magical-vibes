package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControllerCastThreeOrMoreSpellsThisTurn;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TurnFaceDownCommandZoneCardEffect;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "267")
@CardRegistration(set = "MB2", collectorNumber = "503")
public class MarchesasSurpriseParty extends Card {

    public MarchesasSurpriseParty() {
        addEffect(EffectSlot.COMMAND_ZONE_END_STEP_TRIGGERED, new ConditionalEffect(
                new AnyOf(List.of(
                        new ControllerCastThreeOrMoreSpellsThisTurn(null),
                        new OpponentLostLifeThisTurn(6),
                        new GraveyardCardThreshold(9, null))),
                new MayEffect(
                        SequenceEffect.of(new TurnFaceDownCommandZoneCardEffect(), new DrawCardEffect(1)),
                        "Reveal your choice and collect the reward?")));
    }
}
