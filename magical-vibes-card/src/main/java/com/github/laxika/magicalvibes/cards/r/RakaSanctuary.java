package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "APC", collectorNumber = "67")
public class RakaSanctuary extends Card {

    public RakaSanctuary() {
        var controlsWhite = new ControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.WHITE)));
        var controlsBlue = new ControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.BLUE)));
        var controlsWhiteAndBlue = new AllOf(List.of(controlsWhite, controlsBlue));

        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new AnyOf(List.of(controlsWhite, controlsBlue)),
                new DealDamageToTargetCreatureEffect(new FixedIfCondition(controlsWhiteAndBlue, 3, 1))));
    }
}
