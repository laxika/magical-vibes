package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AvatarKyoshi;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "TLA", collectorNumber = "186")
public class TheLegendOfKyoshi extends Card {

    public TheLegendOfKyoshi() {
        setBackFaceCard(new AvatarKyoshi());

        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new DrawCardEffect(new GreatestPowerAmongControlled()));
        addEffect(EffectSlot.SAGA_CHAPTER_II, SequenceEffect.of(
                new EarthbendTargetLandEffect(new CardsInHand(CountScope.CONTROLLER)),
                new GrantSubtypeEffect(CardSubtype.ISLAND, GrantScope.TARGET)));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new ExileSelfAndReturnTransformedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "AvatarKyoshi";
    }
}
