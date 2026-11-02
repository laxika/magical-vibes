package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "68")
@CardRegistration(set = "PC2", collectorNumber = "90")
@CardRegistration(set = "PCA", collectorNumber = "90")
public class EnlistedWurm extends Card {

    public EnlistedWurm() {
        // Cascade: when you cast this spell, dig the library until a nonland card with lesser mana
        // value, may cast it for free, rest to the bottom in a random order (CascadeEffectHandler).
        addTrigger(Triggers.youCastThisSpell(), new CascadeEffect());
    }
}
