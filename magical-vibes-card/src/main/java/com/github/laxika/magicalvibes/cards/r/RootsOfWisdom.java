package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "190")
public class RootsOfWisdom extends Card {

    public RootsOfWisdom() {
        CardPredicate returnableCard = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.LAND),
                new CardSubtypePredicate(CardSubtype.ELF)));

        addEffect(EffectSlot.SPELL, new MillEffect(3, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new NotCondition(new GraveyardCardThreshold(1, returnableCard)),
                new DrawCardEffect()));
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(returnableCard)
                .mandatory(true)
                .build());
    }
}
