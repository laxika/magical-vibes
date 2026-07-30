package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "M12", collectorNumber = "216")
public class ScepterOfEmpires extends Card {

    public ScepterOfEmpires() {
        // {T}: This artifact deals 1 damage to target player or planeswalker. It deals
        // 3 damage instead if you control artifacts named Crown of Empires and Throne of Empires.
        final Condition hasBothPartners = new AllOf(List.of(
                new ControlsPermanent(artifactNamed("Crown of Empires")),
                new ControlsPermanent(artifactNamed("Throne of Empires"))
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new ConditionalEffect(new NotCondition(hasBothPartners),
                                new DealDamageToTargetPlayerOrPlaneswalkerEffect(1)),
                        new ConditionalEffect(hasBothPartners,
                                new DealDamageToTargetPlayerOrPlaneswalkerEffect(3))
                ),
                "{T}: Scepter of Empires deals 1 damage to target player or planeswalker. It deals "
                        + "3 damage instead if you control artifacts named Crown of Empires and Throne of Empires."
        ));
    }

    private static PermanentPredicate artifactNamed(final String name) {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNamedPredicate(name)
        ));
    }
}
