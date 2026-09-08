package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "152")
public class EmptyTheWarrens extends Card {

    public EmptyTheWarrens() {
        // Create two 1/1 red Goblin creature tokens.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(2, "Goblin", 1, 1, CardColor.RED,
                List.of(CardSubtype.GOBLIN), Set.of(), Set.of()));

        // Storm (When you cast this spell, copy it for each spell cast before it this turn.)
        addEffect(EffectSlot.ON_SELF_CAST, new StormEffect());
    }
}
