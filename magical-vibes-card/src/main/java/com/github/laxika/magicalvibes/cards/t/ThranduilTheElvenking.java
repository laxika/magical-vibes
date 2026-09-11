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
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "167")
public class ThranduilTheElvenking extends Card {

    public ThranduilTheElvenking() {
        CardPredicate elf = new CardSubtypePredicate(CardSubtype.ELF);
        addEffect(EffectSlot.STATIC, new GainActivatedAbilitiesOfCardsInControllerGraveyardEffect(elf));

        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardAllOfPredicate(List.of(
                                new CardSupertypePredicate(CardSupertype.LEGENDARY),
                                new CardSubtypePredicate(CardSubtype.ELF))),
                        SequenceEffect.of(
                                new DrawCardEffect(2),
                                new DiscardEffect(1, DiscardRecipient.CONTROLLER))));
    }
}
