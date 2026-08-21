package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ShuffleHandIntoLibraryAndDrawEffect;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "36")
public class WhirlpoolWarrior extends Card {

    public WhirlpoolWarrior() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ShuffleHandIntoLibraryAndDrawEffect(false));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new SacrificeSelfCost(), new ShuffleHandIntoLibraryAndDrawEffect()),
                "{R}, Sacrifice this creature: Each player shuffles the cards from their hand into their library, then draws that many cards."
        ));
    }
}
