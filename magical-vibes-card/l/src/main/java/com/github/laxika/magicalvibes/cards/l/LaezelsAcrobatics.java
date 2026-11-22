package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTiming;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "94")
public class LaezelsAcrobatics extends Card {

    public LaezelsAcrobatics() {
        PermanentPredicate nontokenCreaturesYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsTokenPredicate())));
        FlickerEffect reExileReturnedAtNextEndStep = new FlickerEffect(
                Subjects.returnedThisResolution(),
                null,
                ReturnTiming.AT_STEP,
                TurnStep.END_STEP,
                false,
                null,
                null,
                0,
                false,
                false);

        addSpellEffect(new ExileAllPermanentsEffect(nontokenCreaturesYouControl, true));
        addSpellEffect(new RollD20Effect(
                RegisterDelayedStepTriggerEffect.aboutExiled(Triggers.endStep(ActivePlayer.ANY),
                        new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.EXILE))),
                SequenceEffect.of(
                        new ReturnAllCardsExiledWithSourceEffect(false),
                        reExileReturnedAtNextEndStep)));
    }
}
