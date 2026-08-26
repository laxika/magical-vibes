package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "29")
public class PhoenixDown extends Card {

    public PhoenixDown() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}",
                List.of(
                        new ExileSelfCost(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardMaxManaValuePredicate(4)
                                )))
                                .targetGraveyard(true)
                                .enterTapped(true)
                                .build()
                ),
                "{1}{W}, {T}, Exile this artifact: Return target creature card with mana value 4 or less "
                        + "from your graveyard to the battlefield tapped."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}",
                List.of(new ExileSelfCost(), new ExileTargetPermanentEffect()),
                "{1}{W}, {T}, Exile this artifact: Exile target Skeleton, Spirit, or Zombie.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasAnySubtypePredicate(Set.of(
                                CardSubtype.SKELETON, CardSubtype.SPIRIT, CardSubtype.ZOMBIE
                        )),
                        "Target must be a Skeleton, Spirit, or Zombie"
                )
        ));
    }
}
