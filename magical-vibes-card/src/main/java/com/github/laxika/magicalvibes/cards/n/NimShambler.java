package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "72")
public class NimShambler extends Card {

    public NimShambler() {
        // This creature gets +1/+0 for each artifact you control.
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(
                new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER), new Fixed(0)));

        // Sacrifice a creature: Regenerate this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeCreatureCost(), new RegenerateEffect()),
                "Sacrifice a creature: Regenerate Nim Shambler."
        ));
    }
}
