package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileAllHandsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "M13", collectorNumber = "158")
public class Worldfire extends Card {

    public Worldfire() {
        // Exile all permanents. Exile all cards from all hands and graveyards.
        // Each player's life total becomes 1.
        addEffect(EffectSlot.SPELL, new ExileAllPermanentsEffect(new PermanentTruePredicate()));
        addEffect(EffectSlot.SPELL, new ExileAllHandsEffect());
        addEffect(EffectSlot.SPELL, new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_PLAYERS));
        addEffect(EffectSlot.SPELL, new SetLifeTotalEffect(1, SetLifeTotalRecipient.EACH_PLAYER));
    }
}
