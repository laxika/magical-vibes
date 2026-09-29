package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RandomPlayerThenFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "160")
public class StraxSontaranNurse extends Card {

    public StraxSontaranNurse() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact"),
                        new RandomPlayerThenFightsTargetCreatureEffect()),
                "{2}, {T}, Sacrifice an artifact: Choose a player at random. When you do, Strax fights another target creature that player controls."
        ));

        addEffect(EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE,
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
