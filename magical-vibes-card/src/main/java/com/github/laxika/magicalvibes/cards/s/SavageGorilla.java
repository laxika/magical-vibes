package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "85")
public class SavageGorilla extends Card {

    public SavageGorilla() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}{B}",
                List.of(
                        new SacrificeSelfCost(),
                        new BoostTargetCreatureEffect(-3, -3),
                        new DrawCardEffect(1)
                ),
                "{U}{B}, {T}, Sacrifice this creature: Target creature gets -3/-3 until end of turn. Draw a card.",
                TargetFilters.creature()
        ));
    }
}
