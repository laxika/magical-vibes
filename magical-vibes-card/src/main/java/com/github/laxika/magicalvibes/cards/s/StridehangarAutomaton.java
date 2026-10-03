package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddThopterTokenToArtifactTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "DRC", collectorNumber = "19")
@CardRegistration(set = "DRC", collectorNumber = "35")
public class StridehangarAutomaton extends Card {

    public StridehangarAutomaton() {
        // Thopters you control get +1/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.THOPTER))));

        // Artifact-token creation also creates one additional Thopter token.
        addEffect(EffectSlot.STATIC, new AddThopterTokenToArtifactTokenCreationEffect());
    }
}
