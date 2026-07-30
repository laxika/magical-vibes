package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

@CardRegistration(set = "M12", collectorNumber = "126")
public class ChandrasPhoenix extends Card {

    public ChandrasPhoenix() {
        // Whenever an opponent is dealt damage by a red instant or sorcery spell you control or by a
        // red planeswalker you control, return this card from your graveyard to your hand. The source
        // gating lives in the slot's collector; the effect is a mandatory self-return.
        addEffect(EffectSlot.GRAVEYARD_ON_OPPONENT_DAMAGED_BY_RED_SPELL_OR_PLANESWALKER,
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build());
    }
}
