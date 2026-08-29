package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

public class TempleOfCultivation extends Card {

    public TempleOfCultivation() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}",
                List.of(new TransformSelfEffect()),
                "{2}{G}, {T}: Transform this land. Activate only if you control ten or more permanents and only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new ControlsPermanentCount(10, new PermanentTruePredicate()),
                "Activate only if you control ten or more permanents."
        ));
    }
}
