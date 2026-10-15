package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "M21", collectorNumber = "220")
public class LeafkinAvenger extends Card {

    public LeafkinAvenger() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(
                        ManaColor.GREEN,
                        new PermanentCount(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentPowerAtLeastPredicate(4)
                                )),
                                CountScope.CONTROLLER)
                )),
                "{T}: Add {G} for each creature with power 4 or greater you control."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{7}{R}",
                List.of(new DealDamageToTargetPlayerOrPlaneswalkerEffect(new SourcePower())),
                "{7}{R}: This creature deals damage equal to its power to target player or planeswalker."
        ));
    }
}
