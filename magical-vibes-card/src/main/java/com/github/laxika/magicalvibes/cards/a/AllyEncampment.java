package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "228")
public class AllyEncampment extends Card {

    private static final PermanentPredicate ALLY_YOU_CONTROL = new PermanentAllOfPredicate(List.of(
            new PermanentHasSubtypePredicate(CardSubtype.ALLY),
            new PermanentControlledBySourceControllerPredicate()));

    public AllyEncampment() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(1, ManaSpendRestriction.SUBTYPE_SPELL, CardSubtype.ALLY)),
                "{T}: Add one mana of any color. Spend this mana only to cast an Ally spell."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new SacrificeSelfCost(), ReturnToHandEffect.target(ALLY_YOU_CONTROL)),
                "{1}, {T}, Sacrifice this land: Return target Ally you control to its owner's hand.",
                new PermanentPredicateTargetFilter(ALLY_YOU_CONTROL,
                        "Target must be an Ally you control")
        ));
    }
}
