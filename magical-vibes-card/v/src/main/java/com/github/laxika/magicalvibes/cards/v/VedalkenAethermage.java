package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "61")
public class VedalkenAethermage extends Card {

    public VedalkenAethermage() {
        target(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER),
                "Target must be a Sliver"
        )).addTrigger(Triggers.enters(ObjectRelation.THIS), ReturnToHandEffect.target());

        addActivatedAbility(AbilityZones.HAND, new ActivatedAbility(false, "{3}",
                List.of(new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.WIZARD))),
                "Wizardcycling {3} ({3}, Discard this card: Search your library for a Wizard card, "
                        + "reveal it, put it into your hand, then shuffle.)"));
    }
}
