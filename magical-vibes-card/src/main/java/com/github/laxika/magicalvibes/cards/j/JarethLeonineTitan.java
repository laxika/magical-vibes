package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "43")
public class JarethLeonineTitan extends Card {

    public JarethLeonineTitan() {
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfEffect(7, 7));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new GrantProtectionChoiceUntilEndOfTurnEffect(GrantScope.SELF)),
                "{W}: Jareth gains protection from the color of your choice until end of turn."
        ));
    }
}
