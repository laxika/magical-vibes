package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;

import java.util.List;

@CardRegistration(set = "M12", collectorNumber = "137")
public class GoblinBangchuckers extends Card {

    public GoblinBangchuckers() {
        // The target is chosen when the ability goes on the stack; the flip happens on resolution.
        // A lost flip ignores the target and hits this creature instead.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new FlipCoinWinEffect(
                        new DealDamageToAnyTargetEffect(2),
                        new DealDamageToSourceEffect(2))),
                "{T}: Flip a coin. If you win the flip, Goblin Bangchuckers deals 2 damage to any "
                        + "target. If you lose the flip, Goblin Bangchuckers deals 2 damage to itself."));
    }
}
