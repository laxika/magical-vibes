package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;

/** Back face of Poppet Stitcher. */
public class PoppetFactory extends Card {

    public PoppetFactory() {
        var creatureToken = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsTokenPredicate()));

        addEffect(EffectSlot.STATIC,
                new LosesAllAbilitiesEffect(GrantScope.OWN_CREATURES, creatureToken));
        addEffect(EffectSlot.STATIC,
                new SetBasePowerToughnessEffect(3, 3, GrantScope.OWN_CREATURES, creatureToken));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MayEffect(new TransformSelfEffect(), "Transform Poppet Factory?"));
    }
}
