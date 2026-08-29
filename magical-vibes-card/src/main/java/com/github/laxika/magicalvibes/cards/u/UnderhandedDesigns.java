package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "104")
public class UnderhandedDesigns extends Card {

    public UnderhandedDesigns() {
        addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD, new MayPayManaEffect(
                "{1}",
                SequenceEffect.of(
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(1)
                ),
                "Pay {1} to have each opponent lose 1 life and you gain 1 life?"
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{1}{B}, Sacrifice Underhanded Designs: Destroy target creature. Activate only if you control two or more artifacts.",
                TargetFilters.creature()
        ).withRequiredControlledPermanents(
                new PermanentIsArtifactPredicate(), 2, "two or more artifacts"));
    }
}
