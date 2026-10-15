package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;

import java.util.List;

@CardRegistration(set = "WOC", collectorNumber = "141")
public class NymrisOonasTrickster extends Card {

    public NymrisOonasTrickster() {
        // Whenever you cast your first spell during each opponent's turn, look at the top two
        // cards of your library. Put one into your hand and the other into your graveyard.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(null,
                        List.of(LookAtTopCardsEffect.chooseExactlyNToHandRestToGraveyard(2, 1)),
                        null, null, null, true, false, null, 1));
    }
}
