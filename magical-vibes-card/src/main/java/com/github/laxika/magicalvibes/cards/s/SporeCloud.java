package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "72a")
@CardRegistration(set = "FEM", collectorNumber = "72b")
@CardRegistration(set = "FEM", collectorNumber = "72c")
@CardRegistration(set = "FEM", collectorNumber = "150")
public class SporeCloud extends Card {

    public SporeCloud() {
        addEffect(EffectSlot.SPELL, new TapPermanentsEffect(
                TapUntapScope.ALL_CREATURES, new PermanentIsBlockingPredicate()));
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombat());
        addEffect(EffectSlot.SPELL, new SkipNextUntapEffect(
                TapUntapScope.ALL_CREATURES,
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsAttackingPredicate(),
                        new PermanentIsBlockingPredicate()))));
    }
}
