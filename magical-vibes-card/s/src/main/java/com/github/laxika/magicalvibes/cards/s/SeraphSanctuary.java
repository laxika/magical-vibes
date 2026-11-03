package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "AVR", collectorNumber = "228")
@CardRegistration(set = "DDQ", collectorNumber = "33")
@CardRegistration(set = "SLD", collectorNumber = "733")
@CardRegistration(set = "SLD", collectorNumber = "797")
@CardRegistration(set = "SLD", collectorNumber = "1292")
@CardRegistration(set = "FDC", collectorNumber = "313")
public class SeraphSanctuary extends Card {

    public SeraphSanctuary() {
        // When this land enters, you gain 1 life.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(1));

        // Whenever an Angel you control enters, you gain 1 life.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.ANGEL))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new GainLifeEffect(1));

        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
