package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * Corpse Hauler — {2}{B}, Sacrifice this creature: return another target creature card from your
 * graveyard to your hand.
 *
 * <p>The sacrifice is a cost, so Corpse Hauler is already in the graveyard when the returned card
 * is selected. "Another" is therefore modelled explicitly with a negated
 * {@link CardIsSelfPredicate} so it cannot return itself.
 */
@CardRegistration(set = "M14", collectorNumber = "90")
public class CorpseHauler extends Card {

    public CorpseHauler() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(new SacrificeSelfCost(), ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardNotPredicate(new CardIsSelfPredicate()))))
                        .build()),
                "{2}{B}, Sacrifice Corpse Hauler: Return another target creature card from your graveyard to your hand."
        ));
    }
}
