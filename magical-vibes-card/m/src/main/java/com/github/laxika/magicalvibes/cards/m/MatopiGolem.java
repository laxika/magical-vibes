package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "150")
public class MatopiGolem extends Card {

    public MatopiGolem() {
        // {1}: Regenerate this creature. When it regenerates this way, put a -1/-1 counter on it.
        // The counter is owed only when the shield is actually spent, so it rides on the shield.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(RegenerateEffect.withMinusOneCounterOnRegenerate()),
                "{1}: Regenerate this creature. When it regenerates this way, put a -1/-1 counter on it."
        ));
    }
}
