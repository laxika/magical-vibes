package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ActivationCount;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtEndStepEffect;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "4")
public class FarrelitePriest extends Card {

    public FarrelitePriest() {
        addActivatedAbility(new ActivatedAbility(false, "{1}", List.of(
                new AwardManaEffect(ManaColor.WHITE),
                new ConditionalEffect(new ActivationCount(4, 0), new SacrificeSelfAtEndStepEffect())),
                "{1}: Add {W}. If this ability has been activated four or more times this turn, sacrifice this creature at the beginning of the next end step."));
    }
}
