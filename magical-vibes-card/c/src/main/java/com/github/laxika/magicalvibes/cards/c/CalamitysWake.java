package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastNoncreatureSpellsThisTurnEffect;

@CardRegistration(set = "BRO", collectorNumber = "4")
public class CalamitysWake extends Card {

    public CalamitysWake() {
        addEffect(EffectSlot.SPELL, new ExileGraveyardCardsEffect(GraveyardExileScope.ALL_PLAYERS));
        addEffect(EffectSlot.SPELL, new PlayersCantCastNoncreatureSpellsThisTurnEffect());
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
