package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithAtLeastCardsDrawnThisTurn;
import com.github.laxika.magicalvibes.model.amount.OpponentsWithAtLeastLandsEnteredBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "NCC", collectorNumber = "21")
@CardRegistration(set = "NCC", collectorNumber = "122")
@CardRegistration(set = "MKC", collectorNumber = "84")
public class SmugglersShare extends Card {

    public SmugglersShare() {
        addEffect(EffectSlot.END_STEP_TRIGGERED, SequenceEffect.of(
                new DrawCardEffect(new OpponentsWithAtLeastCardsDrawnThisTurn(2)),
                CreateTokenEffect.ofTreasureToken(
                        new OpponentsWithAtLeastLandsEnteredBattlefieldThisTurn(2))));
    }
}
