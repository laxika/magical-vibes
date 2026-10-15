package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandAndApplyPerpetualKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.Set;

@CardRegistration(set = "YMID", collectorNumber = "5")
public class EtherealEscort extends Card {

    public EtherealEscort() {
        var lifelink = new ChooseCardFromHandAndApplyPerpetualKeywordEffect(
                new CardTruePredicate(), Set.of(Keyword.LIFELINK));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, lifelink);
        addEffect(EffectSlot.ON_ATTACK, lifelink);
    }
}
