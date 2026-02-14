package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;

public class Discombobulate extends Card {

    public Discombobulate() {
        super("Discombobulate", CardType.INSTANT, "{2}{U}{U}", CardColor.BLUE);

        setCardText("Counter target spell. Look at the top four cards of your library, then put them back in any order.");
        setNeedsSpellTarget(true);
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        addEffect(EffectSlot.SPELL, new ReorderTopCardsOfLibraryEffect(4));
    }
}
