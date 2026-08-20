package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDragonApproachAndSearchEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "STX", collectorNumber = "97")
public class DragonsApproach extends Card {

    public DragonsApproach() {
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(3, DamageRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SPELL, new MayEffect(
                new ExileDragonApproachAndSearchEffect(),
                "Exile Dragon's Approach and four cards named Dragon's Approach from your graveyard?"));
    }
}
