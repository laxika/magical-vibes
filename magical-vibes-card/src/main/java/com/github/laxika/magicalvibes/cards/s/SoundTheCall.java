package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "CSP", collectorNumber = "123")
public class SoundTheCall extends Card {

    public SoundTheCall() {
        CardsInGraveyard sameNameCards = new CardsInGraveyard(
                new CardNamedPredicate("Sound the Call"), CountScope.ANY_PLAYER);
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                1, "Wolf", 1, 1, CardColor.GREEN, List.of(CardSubtype.WOLF), Set.of(), Set.of(),
                Map.of(EffectSlot.STATIC, new BoostSelfEffect(sameNameCards, sameNameCards))
        ));
    }
}
