package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentMayDrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "33a")
@CardRegistration(set = "ALL", collectorNumber = "33b")
public class SoldeviHeretic extends Card {

    public SoldeviHeretic() {
        // {W}, {T}: Prevent the next 2 damage that would be dealt to target creature this turn.
        // Target opponent may draw a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(PreventDamageEffect.nextToTargetCreature(2), new TargetOpponentMayDrawCardEffect()),
                "{W}, {T}: Prevent the next 2 damage that would be dealt to target creature this turn. "
                        + "Target opponent may draw a card.",
                TargetFilters.creature()
        ));
    }
}
