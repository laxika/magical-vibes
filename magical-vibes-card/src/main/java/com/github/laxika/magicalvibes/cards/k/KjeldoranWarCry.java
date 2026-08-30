package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

@CardRegistration(set = "CSP", collectorNumber = "13")
public class KjeldoranWarCry extends Card {

    public KjeldoranWarCry() {
        var boost = new Sum(
                new Fixed(1),
                new CardsInGraveyard(new CardNamedPredicate("Kjeldoran War Cry"), CountScope.ANY_PLAYER));
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(boost, boost));
    }
}
