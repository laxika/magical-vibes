package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "APC", collectorNumber = "20")
public class CetaSanctuary extends Card {

    public CetaSanctuary() {
        var controlsRed = new ControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.RED)));
        var controlsGreen = new ControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.GREEN)));
        var controlsRedAndGreen = new AllOf(List.of(controlsRed, controlsGreen));

        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                controlsRedAndGreen,
                SequenceEffect.of(
                        new DrawCardEffect(2),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER))));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new AllOf(List.of(
                        new AnyOf(List.of(controlsRed, controlsGreen)),
                        new NotCondition(controlsRedAndGreen))),
                SequenceEffect.of(
                        new DrawCardEffect(1),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER))));
    }
}
