package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "5")
public class AuriokWindwalker extends Card {

    public AuriokWindwalker() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AttachSourceEquipmentToTargetCreatureEffect(Subjects.target(0), Subjects.target(1))),
                "{T}: Attach target Equipment you control to target creature you control.",
                List.of(
                        new ControlledPermanentPredicateTargetFilter(
                                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                                "Target must be an Equipment you control"),
                        TargetFilters.creatureYouControl()
                ),
                2,
                2
        ));
    }
}
