package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOK", collectorNumber = "24")
public class PromiseOfBunrei extends Card {

    public PromiseOfBunrei() {
        // When a creature you control dies, sacrifice this enchantment. If you do, create four
        // 1/1 colorless Spirit creature tokens.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new SacrificeSelfThenEffect(spiritTokens()));
    }

    private static CreateTokenEffect spiritTokens() {
        return new CreateTokenEffect(4, "Spirit", 1, 1, null,
                List.of(CardSubtype.SPIRIT), Set.of(), Set.of());
    }
}
