package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EachOpponentGainsLifeCastingCost;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "111")
public class ReverentSilence extends Card {

    public ReverentSilence() {
        addCastingOption(new AlternateHandCast(
                List.of(new EachOpponentGainsLifeCastingCost(6)),
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.FOREST)),
                false));
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentIsEnchantmentPredicate()));
    }
}
