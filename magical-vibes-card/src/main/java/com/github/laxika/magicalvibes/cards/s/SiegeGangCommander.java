package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSubtypeCreatureCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "234")
public class SiegeGangCommander extends Card {

    public SiegeGangCommander() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateCreatureTokenEffect(
                3,
                "Goblin", 1, 1, CardColor.RED, List.of(CardSubtype.GOBLIN), Set.of(), Set.of()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new SacrificeSubtypeCreatureCost(CardSubtype.GOBLIN), new DealDamageToAnyTargetEffect(2)),
                true,
                "{1}{R}, Sacrifice a Goblin: Siege-Gang Commander deals 2 damage to any target."
        ));
    }
}
