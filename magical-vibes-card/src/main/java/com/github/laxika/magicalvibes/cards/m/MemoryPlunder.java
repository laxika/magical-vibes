package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;

@CardRegistration(set = "SHM", collectorNumber = "169")
public class MemoryPlunder extends Card {

    public MemoryPlunder() {
        // You may cast target instant or sorcery card from an opponent's graveyard
        // without paying its mana cost.
        addEffect(EffectSlot.SPELL, new CastTargetInstantOrSorceryFromGraveyardEffect(
                GraveyardSearchScope.OPPONENT_GRAVEYARD, true));
    }
}
