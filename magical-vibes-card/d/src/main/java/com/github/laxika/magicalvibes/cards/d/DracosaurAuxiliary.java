package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsSaddled;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeSaddledUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SaddleCost;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "122")
public class DracosaurAuxiliary extends Card {

    public DracosaurAuxiliary() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new SourceIsSaddled(), new DealDamageToAnyTargetEffect(2)));
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new SaddleCost(3), new BecomeSaddledUntilEndOfTurnEffect(GrantScope.SELF)),
                "Saddle 3", ActivationTimingRestriction.SORCERY_SPEED));
    }
}
