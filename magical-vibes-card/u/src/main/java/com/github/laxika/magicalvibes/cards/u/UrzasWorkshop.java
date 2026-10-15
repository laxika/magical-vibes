package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "BRC", collectorNumber = "28")
@CardRegistration(set = "BRC", collectorNumber = "51")
public class UrzasWorkshop extends Card {

    public UrzasWorkshop() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // Metalcraft — {T}: Add {C} for each Urza's land you control.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS,
                        new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.URZAS), CountScope.CONTROLLER))),
                "Metalcraft — {T}: Add {C} for each Urza's land you control. Activate only if you control three or more artifacts.",
                ActivationTimingRestriction.METALCRAFT
        ));
    }
}
