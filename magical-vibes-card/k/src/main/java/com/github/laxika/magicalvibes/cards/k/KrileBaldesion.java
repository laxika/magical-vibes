package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "86")
@CardRegistration(set = "FIC", collectorNumber = "176")
public class KrileBaldesion extends Card {

    public KrileBaldesion() {
        // Trace Aether — whenever you cast a noncreature spell, you may return a creature card
        // with mana value equal to that spell's mana value from your graveyard to your hand.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new OncePerTurnTriggerEffect(
                new SpellCastTriggerEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        List.of(new MayEffect(
                                ReturnCardFromGraveyardEffect.builder()
                                        .destination(GraveyardChoiceDestination.HAND)
                                        .filter(new CardTypePredicate(CardType.CREATURE))
                                        .targetGraveyard(true)
                                        .requiresManaValueEqualsX(true)
                                        .build(),
                                "Return target creature card with mana value equal to that spell's mana value from your graveyard to your hand?"))
                )
        ));
    }
}
