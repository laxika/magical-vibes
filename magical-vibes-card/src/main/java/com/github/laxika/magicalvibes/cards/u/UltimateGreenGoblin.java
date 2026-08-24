package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.condition.CardDiscardedThisTurn;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "157")
public class UltimateGreenGoblin extends Card {

    public UltimateGreenGoblin() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new DiscardEffect(1, DiscardRecipient.CONTROLLER),
                CreateTokenEffect.ofTreasureToken(1)));
        addCastingOption(new GraveyardCast(null, "{2}{B/R}", List.of(), new CardDiscardedThisTurn()));
    }
}
