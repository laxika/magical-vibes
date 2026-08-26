package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MKM", collectorNumber = "70")
public class ReenactTheCrime extends Card {

    public ReenactTheCrime() {
        addEffect(EffectSlot.SPELL, new ExileTargetCardFromGraveyardAndMayCastCopyEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                GraveyardSearchScope.ALL_GRAVEYARDS,
                0,
                true));
    }
}
