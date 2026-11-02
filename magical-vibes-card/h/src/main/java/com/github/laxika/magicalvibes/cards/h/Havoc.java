package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.SpellCastLifeDrainEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMP", collectorNumber = "181")
public class Havoc extends Card {

    public Havoc() {
        // Whenever an opponent casts a white spell, they lose 2 life.
        addTrigger(Triggers.opponentCastsSpell(),
                new SpellCastLifeDrainEffect(2, 0, new CardColorPredicate(CardColor.WHITE)));
    }
}
