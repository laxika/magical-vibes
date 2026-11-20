package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ISD", collectorNumber = "18")
public class GhostlyPossession extends Card {

    public GhostlyPossession() {
        target(TargetFilters.creature()).addStaticEffect(new GrantKeywordEffect(Keyword.FLYING, Subjects.attachedCreature()))
          .addStaticEffect(new PreventAllCombatDamageToAndByEnchantedCreatureEffect());
    }
}
