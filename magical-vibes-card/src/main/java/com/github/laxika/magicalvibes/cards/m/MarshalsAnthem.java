package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "WWK", collectorNumber = "15")
public class MarshalsAnthem extends Card {

    public MarshalsAnthem() {
        addEffect(EffectSlot.SPELL, RepeatableAdditionalManaCost.multikicker(List.of("{1}{W}")));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                new CardTypePredicate(CardType.CREATURE), new RepeatedAdditionalCostCount("{1}{W}")));
    }
}
