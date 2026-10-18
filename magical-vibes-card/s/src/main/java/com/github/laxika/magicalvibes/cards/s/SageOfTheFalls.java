package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "ELD", collectorNumber = "63")
public class SageOfTheFalls extends Card {

    public SageOfTheFalls() {
        MayEffect loot = new MayEffect(SequenceEffect.of(
                new DrawCardEffect(1),
                new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "You may draw a card. If you do, discard a card.");
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, loot);
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardNotPredicate(new CardSubtypePredicate(CardSubtype.HUMAN)), loot));
    }
}
