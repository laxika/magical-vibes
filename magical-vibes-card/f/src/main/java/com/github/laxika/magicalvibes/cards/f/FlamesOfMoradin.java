package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAlternateCastEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "52")
public class FlamesOfMoradin extends Card {

    public FlamesOfMoradin() {
        target(TargetFilters.artifact(), 0, 3)
                .addSpellEffect(DestroyTargetPermanentEffect.eachTargetMatching(new PermanentIsArtifactPredicate()));
        addSpellEffect(ConjureCardsEffect.duplicateOf(Subjects.departedThisWay(StepKind.DESTROY),
                        ConjureDestination.intoHand())
                .withAbilities(List.of(new TriggeredAbility(Triggers.endStep(ActivePlayer.YOU),
                        new SacrificeSelfEffect()))));
        addSpellEffect(PerpetuallyGrantAlternateCastEffect.ratherThanManaCost(
                Subjects.cardsThisWay(StepKind.CONJURE), new ManaCastingCost("{R}")));
    }
}
