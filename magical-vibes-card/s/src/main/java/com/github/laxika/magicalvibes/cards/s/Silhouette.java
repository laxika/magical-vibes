package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToTargetCreatureFromTargetingSpellOrAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LEG", collectorNumber = "77")
public class Silhouette extends Card {

    public Silhouette() {
        target(TargetFilters.creature()).addSpellEffect(new PreventDamageToTargetCreatureFromTargetingSpellOrAbilityEffect());
    }
}
