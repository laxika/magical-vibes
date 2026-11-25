package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "290")
public class SorinVampireLord extends Card {

    public SorinVampireLord() {
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new BoostTargetCreatureEffect(2, 0)),
                "+1: Up to one target creature gets +2/+0 until end of turn.",
                TargetFilters.creature(),
                +1, null, null,
                List.of(), 0, 1
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(DealDamageEffect.toAnyTarget(4), new GainLifeEffect(4)),
                "\u22122: Sorin deals 4 damage to any target. You gain 4 life."
        ));

        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                true,
                                null,
                                List.of(new GainControlOfTargetEffect(Duration.PERMANENT)),
                                "{T}: Gain control of target creature.",
                                TargetFilters.creature()
                        ),
                        Subjects.otherCreaturesYouControl(),
                        new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE),
                        Duration.UNTIL_END_OF_TURN
                )),
                "\u22128: Until end of turn, each Vampire you control gains \"{T}: Gain control of target creature.\""
        ));
    }
}
