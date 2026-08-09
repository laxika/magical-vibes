package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import java.util.List;

@CardRegistration(set = "UDS", collectorNumber = "83")
public class GoblinFestival extends Card {

    public GoblinFestival() {
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new DealDamageToAnyTargetEffect(1),
                        new FlipCoinWinEffect(null, new ChooseOpponentGainsControlOfSourceEffect())),
                "{2}: This enchantment deals 1 damage to any target. Flip a coin. If you lose the flip, "
                        + "choose one of your opponents. That player gains control of this enchantment."));
    }
}
