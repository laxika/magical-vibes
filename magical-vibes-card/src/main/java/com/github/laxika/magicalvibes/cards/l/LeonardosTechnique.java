package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "18")
@CardRegistration(set = "TMT", collectorNumber = "224")
public class LeonardosTechnique extends Card {

    public LeonardosTechnique() {
        addSneak("{1}{W}");

        CardAllOfPredicate creatureCards = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardMaxManaValuePredicate(3)
        ));
        addEffect(EffectSlot.SPELL,
                ReturnTargetCardsFromGraveyardToBattlefieldEffect.withTargetBounds(creatureCards, 2, 1));
    }
}
