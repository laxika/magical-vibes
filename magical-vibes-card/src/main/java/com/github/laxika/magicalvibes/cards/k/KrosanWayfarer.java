package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "123")
public class KrosanWayfarer extends Card {

    public KrosanWayfarer() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new MayEffect(
                                new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.LAND), "land"),
                                "Put a land card from your hand onto the battlefield?")),
                "Sacrifice Krosan Wayfarer: You may put a land card from your hand onto the battlefield."
        ));
    }
}
