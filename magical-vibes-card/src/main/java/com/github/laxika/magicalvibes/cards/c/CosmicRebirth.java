package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.TargetGraveyardCardManaValueAtLeast;
import com.github.laxika.magicalvibes.model.condition.TargetGraveyardCardManaValueAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "MAT", collectorNumber = "28")
public class CosmicRebirth extends Card {

    public CosmicRebirth() {
        ReturnCardFromGraveyardEffect toBattlefield = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardIsPermanentPredicate())
                .targetGraveyard(true)
                .build();
        ReturnCardFromGraveyardEffect toHand = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardIsPermanentPredicate())
                .targetGraveyard(true)
                .build();

        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new TargetGraveyardCardManaValueAtMost(3),
                new MayEffect(toBattlefield, "Put it onto the battlefield?", toHand)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new TargetGraveyardCardManaValueAtLeast(4), toHand));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(3));
    }
}
