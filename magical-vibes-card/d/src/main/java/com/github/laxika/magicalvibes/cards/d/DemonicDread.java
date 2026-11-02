package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "38")
public class DemonicDread extends Card {

    public DemonicDread() {
        // Cascade: when you cast this spell, dig the library until a nonland card with lesser mana
        // value, may cast it for free, rest to the bottom in a random order (CascadeEffectHandler).
        addTrigger(Triggers.youCastThisSpell(), new CascadeEffect());

        // Target creature can't block this turn (target declared by the effect's own targetSpec()).
        addSpellEffect(new CantBlockThisTurnEffect(TapUntapScope.TARGET));
    }
}
