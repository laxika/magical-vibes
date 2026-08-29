package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CastForSpectacleCost;
import com.github.laxika.magicalvibes.model.condition.OpponentLostLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "RNA", collectorNumber = "196")
public class RafterDemon extends Card {

    public RafterDemon() {
        addCastingOption(AlternateHandCast.spectacle("{3}{B}{R}", new OpponentLostLifeThisTurn(1)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new CastForSpectacleCost(), new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT)));
    }
}
