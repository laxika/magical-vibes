package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "KLD", collectorNumber = "97")
public class OvalchaseDaredevil extends Card {

    public OvalchaseDaredevil() {
        // Whenever an artifact you control enters, you may return this card from your graveyard to your hand.
        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardTypePredicate(CardType.ARTIFACT),
                        new MayEffect(ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build(),
                                "Return Ovalchase Daredevil from your graveyard to your hand?")));
    }
}
