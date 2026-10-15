package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "APC", collectorNumber = "74")
public class AnaSanctuary extends Card {

    public AnaSanctuary() {
        var controlsBlue = new ControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.BLUE)));
        var controlsBlack = new ControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.BLACK)));
        var controlsBlueAndBlack = new AllOf(List.of(controlsBlue, controlsBlack));

        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new AnyOf(List.of(controlsBlue, controlsBlack)),
                new BoostTargetCreatureEffect(
                        new FixedIfCondition(controlsBlueAndBlack, 5, 1),
                        new FixedIfCondition(controlsBlueAndBlack, 5, 1))));
    }
}
