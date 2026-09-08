package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "170")
public class AwakenTheHonoredDead extends Card {

    public AwakenTheHonoredDead() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new DestroyTargetPermanentEffect(new PermanentNotPredicate(new PermanentIsLandPredicate())));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new MillEffect(3, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new MayEffect(
                new DiscardCardThenEffect(
                        null,
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardAnyOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardTypePredicate(CardType.LAND))))
                                .targetGraveyard(true)
                                .build(),
                        "a card"),
                "Discard a card to return target creature or land card from your graveyard to your hand?"));
    }
}
