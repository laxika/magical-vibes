package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastCardsExiledWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "23")
public class IdolOfEndurance extends Card {

    public IdolOfEndurance() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                ExileGraveyardCardsEffect.ownAllMatchingUntilSourceLeaves(new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardMaxManaValuePredicate(3)
                ))));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}",
                List.of(new AllowCastCardsExiledWithSourceUntilEndOfTurnEffect(
                        new CardTypePredicate(CardType.CREATURE), true)),
                "{1}{W}, {T}: Until end of turn, you may cast a creature spell from among cards exiled "
                        + "with this artifact without paying its mana cost."
        ));
    }
}
