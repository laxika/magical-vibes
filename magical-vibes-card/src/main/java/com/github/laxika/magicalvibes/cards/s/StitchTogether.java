package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "JUD", collectorNumber = "72")
public class StitchTogether extends Card {

    public StitchTogether() {
        GraveyardCardThreshold threshold = new GraveyardCardThreshold(7, null);
        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);

        addEffect(EffectSlot.SPELL, new ConditionalEffect(threshold,
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(creature)
                        .targetGraveyard(true)
                        .build()));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new NotCondition(threshold),
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(creature)
                        .targetGraveyard(true)
                        .build()));
    }
}
