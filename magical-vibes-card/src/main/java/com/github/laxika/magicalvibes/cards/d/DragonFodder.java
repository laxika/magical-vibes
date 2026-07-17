package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ALA", collectorNumber = "97")
public class DragonFodder extends Card {

    public DragonFodder() {
        // Create two 1/1 red Goblin creature tokens.
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(2, "Goblin", 1, 1, CardColor.RED,
                List.of(CardSubtype.GOBLIN), Set.of(), Set.of()));
    }
}
