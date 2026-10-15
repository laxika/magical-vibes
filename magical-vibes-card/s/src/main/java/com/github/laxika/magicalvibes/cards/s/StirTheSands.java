package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "110")
public class StirTheSands extends Card {

    public StirTheSands() {
        // Create three 2/2 black Zombie creature tokens.
        addEffect(EffectSlot.SPELL, CreateTokenEffect.blackZombie(3));

        // Cycling {3}{B} ({3}{B}, Discard this card: Draw a card.) — discard cost is intrinsic.
        // When you cycle this card, create a 2/2 black Zombie creature token — reflexive cycle
        // trigger folded into the cycling ability, resolving before the cycling draw.
        addHandActivatedAbility(new ActivatedAbility(false, "{3}{B}",
                List.of(CreateTokenEffect.blackZombie(1), new DrawCardEffect(1)),
                "Cycling {3}{B} ({3}{B}, Discard this card: Draw a card.)"));
    }
}
