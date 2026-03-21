package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.KickerReplacementEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "178")
public class SaprolingMigration extends Card {

    public SaprolingMigration() {
        // Kicker {4}
        addEffect(EffectSlot.STATIC, new KickerEffect("{4}"));
        // Create two 1/1 green Saproling creature tokens.
        // If this spell was kicked, create four of those tokens instead.
        addEffect(EffectSlot.SPELL, new KickerReplacementEffect(
                new CreateCreatureTokenEffect(2, "Saproling", 1, 1,
                        CardColor.GREEN, List.of(CardSubtype.SAPROLING),
                        Set.of(), Set.of()),
                new CreateCreatureTokenEffect(4, "Saproling", 1, 1,
                        CardColor.GREEN, List.of(CardSubtype.SAPROLING),
                        Set.of(), Set.of())
        ));
    }
}
