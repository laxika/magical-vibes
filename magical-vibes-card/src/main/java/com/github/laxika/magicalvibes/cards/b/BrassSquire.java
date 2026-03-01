package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "101")
public class BrassSquire extends Card {

    public BrassSquire() {
        // {T}: Attach target Equipment you control to target creature you control.
        addActivatedAbility(new ActivatedAbility(
                true,  // requires tap
                null,  // no mana cost
                List.of(new AttachTargetEquipmentToTargetCreatureEffect()),
                "{T}: Attach target Equipment you control to target creature you control.",
                List.of(
                        new ControlledPermanentPredicateTargetFilter(new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT), "Target must be an Equipment you control"),
                        new ControlledPermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "Target must be a creature you control")
                ),
                2,  // minTargets
                2   // maxTargets
        ));
    }
}
