package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ALA", collectorNumber = "211")
public class MinionReflector extends Card {

    public MinionReflector() {
        // Whenever a nontoken creature you control enters, you may pay {2}. If you do, create a token
        // that's a copy of that creature, except it has haste and "At the beginning of the end step,
        // sacrifice this permanent."
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD, new MayPayManaEffect(
                "{2}",
                new CreateTokenCopyOfTargetPermanentEffect(
                        List.of(), Set.of(), null, null, Map.of(),
                        false, false, false, false,
                        false, false, null, Set.of(Keyword.HASTE),
                        false, Map.of(EffectSlot.END_STEP_TRIGGERED, List.<CardEffect>of(new SacrificeSelfEffect())),
                        List.of(), false, false,
                        new Fixed(1), false, Set.of()),
                "Pay {2} to create a token that's a copy of that creature (with haste, sacrificed at end step)?"
        ));
    }
}
