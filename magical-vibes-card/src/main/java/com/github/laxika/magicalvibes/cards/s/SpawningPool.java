package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AnimateLandEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "358")
public class SpawningPool extends Card {

    public SpawningPool() {
        setEntersTapped(true);
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.BLACK));
        // {1}{B}: Spawning Pool becomes a 1/1 black Skeleton creature with "{B}: Regenerate this creature" until end of turn. It's still a land.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new AnimateLandEffect(1, 1, List.of(CardSubtype.SKELETON), Set.of(), CardColor.BLACK)),
                "{1}{B}: Spawning Pool becomes a 1/1 black Skeleton creature with \"{B}: Regenerate this creature\" until end of turn. It's still a land."
        ));
        // {B}: Regenerate this creature. (Only available while animated as a creature)
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new RegenerateEffect()),
                "{B}: Regenerate this creature.",
                ActivationTimingRestriction.ONLY_WHILE_CREATURE
        ));
    }
}
