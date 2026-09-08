package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "APC", collectorNumber = "5")
public class DegaSanctuary extends Card {

    public DegaSanctuary() {
        var controlsBlack = new ControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.BLACK)));
        var controlsRed = new ControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.RED)));
        var controlsBlackAndRed = new AllOf(List.of(controlsBlack, controlsRed));

        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                controlsBlackAndRed, new GainLifeEffect(4)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new AllOf(List.of(
                        new AnyOf(List.of(controlsBlack, controlsRed)),
                        new NotCondition(controlsBlackAndRed))),
                new GainLifeEffect(2)));
    }
}
