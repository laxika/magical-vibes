package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "SPM", collectorNumber = "177")
public class SteelWreckingBall extends Card {

    public SteelWreckingBall() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToTargetCreatureEffect(5));

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new DestroyTargetPermanentEffect(new PermanentIsArtifactPredicate())),
                "{1}{R}, Discard this card: Destroy target artifact.",
                TargetFilters.artifact()
        ));
    }
}
