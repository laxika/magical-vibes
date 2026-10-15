package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "81")
public class UnwelcomeSprite extends Card {

    public UnwelcomeSprite() {
        // Flying (auto-loaded from Scryfall).
        // Whenever you cast a spell during an opponent's turn, surveil 2.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(null, List.of(new SurveilEffect(2)), true));
    }
}
