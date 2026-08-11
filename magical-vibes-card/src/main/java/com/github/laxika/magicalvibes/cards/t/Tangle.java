package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "INV", collectorNumber = "213")
public class Tangle extends Card {

    public Tangle() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombat());
        addEffect(EffectSlot.SPELL, new SkipNextUntapEffect(
                TapUntapScope.ALL_CREATURES, new PermanentIsAttackingPredicate()));
    }
}
