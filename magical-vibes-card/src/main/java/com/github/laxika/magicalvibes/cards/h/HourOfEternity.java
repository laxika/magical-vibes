package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileCreaturesFromGraveyardAndCreateTokensEffect;

@CardRegistration(set = "HOU", collectorNumber = "36")
public class HourOfEternity extends Card {

    public HourOfEternity() {
        // Exile X target creature cards from your graveyard. For each card exiled this way, create a
        // token that's a copy of that card, except it's a 4/4 black Zombie.
        addEffect(EffectSlot.SPELL, new ExileCreaturesFromGraveyardAndCreateTokensEffect(
                false, true, 4, 4, CardColor.BLACK, CardSubtype.ZOMBIE));
    }
}
