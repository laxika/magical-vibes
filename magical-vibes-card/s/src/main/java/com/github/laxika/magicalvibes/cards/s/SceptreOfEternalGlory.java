package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentsWithSameName;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "40K", collectorNumber = "166")
public class SceptreOfEternalGlory extends Card {

    public SceptreOfEternalGlory() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(3)),
                "{T}: Add three mana of any one color. Activate only if you control three or more lands with the same name."
        ).withActivationCondition(
                new ControlsPermanentsWithSameName(3, new PermanentIsLandPredicate()),
                "Activate only if you control three or more lands with the same name."
        ));
    }
}
