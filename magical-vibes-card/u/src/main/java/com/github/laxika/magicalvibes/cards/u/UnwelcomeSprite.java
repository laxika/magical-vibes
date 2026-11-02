package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "81")
public class UnwelcomeSprite extends Card {

    public UnwelcomeSprite() {
        // Flying (auto-loaded from Scryfall).
        // Whenever you cast a spell during an opponent's turn, surveil 2.
        addTrigger(Triggers.youCastSpell(),
                new SpellCastTriggerEffect(null, List.of(new SurveilEffect(2)), true));
    }
}
