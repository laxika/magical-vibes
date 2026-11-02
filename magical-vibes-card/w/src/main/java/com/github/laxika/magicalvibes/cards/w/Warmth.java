package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "52")
@CardRegistration(set = "TMP", collectorNumber = "51")
public class Warmth extends Card {

    public Warmth() {
        // Whenever an opponent casts a red spell, you gain 2 life.
        addTrigger(Triggers.opponentCastsSpell(),
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.RED), List.of(new GainLifeEffect(2))));
    }
}
