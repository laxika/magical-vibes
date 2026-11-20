package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "195")
@CardRegistration(set = "CSP", collectorNumber = "79")
public class Cryoclasm extends Card {

    public Cryoclasm() {
        // Destroy target Plains or Island. Cryoclasm deals 3 damage to that land's controller.
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.PLAINS),
                        new PermanentHasSubtypePredicate(CardSubtype.ISLAND))),
                "Target must be a Plains or Island"
        ))
                .addSpellEffect(new DestroyTargetPermanentEffect())
                        .addSpellEffect(new DealDamageToPlayersEffect(3, Subjects.controllerOf(Subjects.target())));
    }
}
