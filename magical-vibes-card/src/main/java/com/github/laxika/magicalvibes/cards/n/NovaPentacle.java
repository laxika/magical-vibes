package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageFromChosenSourceToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "289")
public class NovaPentacle extends Card {

    public NovaPentacle() {
        ActivatedAbility ability = new ActivatedAbility(
                true,
                "{3}",
                List.of(new RedirectNextDamageFromChosenSourceToTargetCreatureEffect()),
                "{3}, {T}: The next time a source of your choice would deal damage to you this turn, "
                        + "that damage is dealt to target creature of an opponent's choice instead.",
                TargetFilters.creature());
        ability.withOpponentChosenTargetByController(0, TargetFilters.creature());
        addActivatedAbility(ability);
    }
}
