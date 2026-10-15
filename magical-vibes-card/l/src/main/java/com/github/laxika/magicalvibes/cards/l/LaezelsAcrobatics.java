package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerScope;
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
        FlickerEffect returnAtNextEndStep = new FlickerEffect(
                FlickerScope.CONTROLLERS_PERMANENTS,
                nontokenCreaturesYouControl,
                ReturnTiming.AT_STEP,
                TurnStep.END_STEP,
                false,
                null,
                null,
                0,
                false,
                false);
        FlickerEffect reExileReturnedAtNextEndStep = new FlickerEffect(
                FlickerScope.RETURNED_PERMANENTS,
                null,
                ReturnTiming.AT_STEP,
                TurnStep.END_STEP,
                false,
                null,
                null,
                0,
                false,
                false);

        addEffect(EffectSlot.SPELL, new RollD20Effect(
                returnAtNextEndStep,
                SequenceEffect.of(
                        new ExileAllPermanentsEffect(nontokenCreaturesYouControl, true),
                        new ReturnAllCardsExiledWithSourceEffect(false),
                        reExileReturnedAtNextEndStep)));
    }
}
