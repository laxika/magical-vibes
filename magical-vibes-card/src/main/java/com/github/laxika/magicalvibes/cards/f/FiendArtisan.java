package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "220")
public class FiendArtisan extends Card {

    public FiendArtisan() {
        CardsInGraveyard creatureCards = new CardsInGraveyard(
                new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(creatureCards, creatureCards));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{B/G}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new SearchLibraryEffect(
                                new CardTypePredicate(CardType.CREATURE),
                                LibrarySearchDestination.BATTLEFIELD,
                                new ManaValueBound(false, 0))
                ),
                "{X}{B/G}, {T}, Sacrifice another creature: Search your library for a creature card "
                        + "with mana value X or less, put it onto the battlefield, then shuffle. Activate "
                        + "only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
