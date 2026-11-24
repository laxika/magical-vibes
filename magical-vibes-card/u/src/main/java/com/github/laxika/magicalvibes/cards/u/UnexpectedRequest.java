package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.effect.UnattachEquipmentFromTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FIN", collectorNumber = "167")
public class UnexpectedRequest extends Card {

    public UnexpectedRequest() {
        target(TargetFilters.creature())
                .addSpellEffect(new GainControlOfTargetEffect(Duration.UNTIL_END_OF_TURN))
                .addSpellEffect(new UntapPermanentsEffect(Subjects.target()))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, Subjects.target()))
                .addSpellEffect(new MayEffect(
                        SequenceEffect.of(
                                ChoosePermanentsEffect.exactly(1, Subjects.controlledBy(Subjects.you(),
                                        new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT)), "an Equipment you control")
                                        .thatCanBeAttachedTo(Subjects.target()),
                                new AttachSourceEquipmentToTargetCreatureEffect(Subjects.thisWay(StepKind.CHOOSE), Subjects.target()),
                                new ConditionalEffect(new HappenedThisWay(StepKind.ATTACH),
                                        RegisterDelayedStepTriggerEffect.aboutAtNext(Triggers.endStep(ActivePlayer.ANY),
                                                Subjects.thisWay(StepKind.ATTACH),
                                                new UnattachEquipmentFromTargetPermanentsEffect(Subjects.triggering())))),
                        "Attach an Equipment you control to that creature?"));
    }
}
