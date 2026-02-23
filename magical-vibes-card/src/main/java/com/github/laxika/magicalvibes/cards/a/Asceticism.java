package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeTargetOfSpellsOrAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect.Scope;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "110")
public class Asceticism extends Card {

    public Asceticism() {
        addEffect(EffectSlot.STATIC, new GrantEffect(new CantBeTargetOfSpellsOrAbilitiesEffect(), Scope.OWN_CREATURES));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new RegenerateEffect(true)),
                "{1}{G}: Regenerate target creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));
    }
}
