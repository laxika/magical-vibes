package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfCardsInControllerGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "167")
public class ThranduilTheElvenking extends Card {

    public ThranduilTheElvenking() {
        CardPredicate elf = new CardSubtypePredicate(CardSubtype.ELF);
        addStaticEffect(new GainActivatedAbilitiesOfCardsInControllerGraveyardEffect(elf));

        addEffect(EffectSlot.ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                                new PermanentHasSubtypePredicate(CardSubtype.ELF))),
                        SequenceEffect.of(
                                new DrawCardEffect(2),
                                new DiscardEffect(1, DiscardRecipient.CONTROLLER))));
    }
}
