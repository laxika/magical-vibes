package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.filter.CardHasFlashbackPredicate;

@CardRegistration(set = "ODY", collectorNumber = "165")
public class Tombfire extends Card {

    public Tombfire() {
        addEffect(EffectSlot.SPELL, new ExileGraveyardCardsEffect(
                0, GraveyardExileScope.TARGET_PLAYER_ALL_MATCHING, new CardHasFlashbackPredicate()));
    }
}
