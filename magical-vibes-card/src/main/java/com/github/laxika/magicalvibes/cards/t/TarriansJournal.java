package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardHandCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "126")
@CardRegistration(set = "LCI", collectorNumber = "371")
public class TarriansJournal extends Card {

    public TarriansJournal() {
        setBackFaceCard(new TheTombOfAclazotz());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsArtifactPredicate(),
                                        new PermanentIsCreaturePredicate())),
                                "Sacrifice another artifact or creature"),
                        new DrawCardEffect(1)
                ),
                "{T}, Sacrifice another artifact or creature: Draw a card. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new DiscardHandCost(), new TransformSelfEffect()),
                "{2}, {T}, Discard your hand: Transform Tarrian's Journal. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "TheTombOfAclazotz";
    }
}
