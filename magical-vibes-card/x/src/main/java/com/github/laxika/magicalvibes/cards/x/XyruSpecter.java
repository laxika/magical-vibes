package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.RevealAnyNumberOfCardsFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesOneEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "MB1", collectorNumber = "51")
public class XyruSpecter extends Card {

    public XyruSpecter() {
        addEffect(EffectSlot.ON_DAMAGE_TO_OPPONENT,
                TargetPlayerChoosesOneEffect.forTargetedPlayer(List.of(
                        new ChooseOneEffect.ChooseOneOption("Discard a card",
                                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER)),
                        new ChooseOneEffect.ChooseOneOption("Challenge", SequenceEffect.of(
                                new RevealAnyNumberOfCardsFromHandEffect(
                                        new CardColorPredicate(CardColor.BLACK)),
                                ConditionalEffect.unless(new EventValueAtLeast(2),
                                        new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER)))))));
    }
}
