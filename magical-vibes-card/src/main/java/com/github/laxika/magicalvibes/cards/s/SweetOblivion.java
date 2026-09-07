package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "70")
public class SweetOblivion extends Card {

    public SweetOblivion() {
        addEffect(EffectSlot.SPELL, new MillEffect(4, MillRecipient.TARGET_PLAYER));
        addCastingOption(new GraveyardCast(null, "{3}{U}",
                List.of(new ExileNCardsFromGraveyardCastingCost(null, "other cards", 4)),
                null, false, true));
    }
}
