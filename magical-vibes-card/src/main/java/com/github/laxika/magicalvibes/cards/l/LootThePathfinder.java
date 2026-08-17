package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "212")
public class LootThePathfinder extends Card {

    public LootThePathfinder() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new AwardAnyColorManaEffect(3)),
                "Exhaust — {G}, {T}: Add three mana of any one color. (Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new DrawCardEffect(3)),
                "Exhaust — {U}, {T}: Draw three cards. (Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new DealDamageToAnyTargetEffect(3)),
                "Exhaust — {R}, {T}: Loot deals 3 damage to any target. (Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());
    }
}
