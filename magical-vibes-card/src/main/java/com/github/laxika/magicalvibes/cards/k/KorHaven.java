package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "NEM", collectorNumber = "141")
public class KorHaven extends Card {

    public KorHaven() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {1}{W}, {T}: Prevent all combat damage that would be dealt by target attacking creature this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}",
                List.of(PreventDamageEffect.allCombatByTargetCreatures()),
                "{1}{W}, {T}: Prevent all combat damage that would be dealt by target attacking creature this turn.",
                TargetFilters.attackingCreature()
        ));
    }
}
