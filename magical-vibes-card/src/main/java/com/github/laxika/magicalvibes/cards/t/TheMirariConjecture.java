package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.GrantInstantSorceryCopyUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * The Mirari Conjecture — {4}{U} Enchantment — Saga
 *
 * (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
 * I — Return target instant card from your graveyard to your hand.
 * II — Return target sorcery card from your graveyard to your hand.
 * III — Until end of turn, whenever you cast an instant or sorcery spell, copy it.
 *        You may choose new targets for the copy.
 */
@CardRegistration(set = "DOM", collectorNumber = "57")
public class TheMirariConjecture extends Card {

    public TheMirariConjecture() {
        // Chapter I: Return target instant card from your graveyard to your hand
        addEffect(EffectSlot.SAGA_CHAPTER_I, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.INSTANT))
                .targetGraveyard(true)
                .build());

        // Chapter II: Return target sorcery card from your graveyard to your hand
        addEffect(EffectSlot.SAGA_CHAPTER_II, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.SORCERY))
                .targetGraveyard(true)
                .build());

        // Chapter III: Until end of turn, whenever you cast an instant or sorcery spell,
        // copy it. You may choose new targets for the copy.
        addEffect(EffectSlot.SAGA_CHAPTER_III, new GrantInstantSorceryCopyUntilEndOfTurnEffect());
    }
}
