package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "144")
public class GruulGuildmage extends Card {

    public GruulGuildmage() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsLandPredicate(), "Sacrifice a land", false),
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(2)
                ),
                "{3}{R}, Sacrifice a land: This creature deals 2 damage to target player or planeswalker."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(new BoostTargetCreatureEffect(2, 2)),
                "{3}{G}: Target creature gets +2/+2 until end of turn."
        ));
    }
}
