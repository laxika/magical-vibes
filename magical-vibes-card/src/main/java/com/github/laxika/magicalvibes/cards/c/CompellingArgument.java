package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "47")
public class CompellingArgument extends Card {

    public CompellingArgument() {
        // Target player mills five cards.
        addEffect(EffectSlot.SPELL, new MillEffect(5, MillRecipient.TARGET_PLAYER));

        // Cycling {U} ({U}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addHandActivatedAbility(new ActivatedAbility(false, "{U}",
                List.of(new DrawCardEffect(1)),
                "Cycling {U} ({U}, Discard this card: Draw a card.)"));
    }
}
