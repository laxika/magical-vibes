package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;

@CardRegistration(set = "INR", collectorNumber = "14b")
@CardRegistration(set = "V17", collectorNumber = "16")
public class BriselaVoiceOfNightmares extends Card {

    public BriselaVoiceOfNightmares() {
        // Your opponents can't cast spells with mana value 3 or less.
        addStaticEffect(new OpponentsCantCastSpellsMatchingPredicateEffect(new CardMaxManaValuePredicate(3, true)));
    }
}
