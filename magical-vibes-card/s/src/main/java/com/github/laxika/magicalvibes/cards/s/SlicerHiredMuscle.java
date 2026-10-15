package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeSacrificedEffect;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantStaticEffectToSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourcePermanentUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;

@CardRegistration(set = "BOT", collectorNumber = "6")
@CardRegistration(set = "BOT", collectorNumber = "21")
public class SlicerHiredMuscle extends Card {

    public SlicerHiredMuscle() {
        setBackFaceCard(new SlicerHighSpeedAntagonist());
        addCastingOption(AlternateHandCast.moreThanMeetsTheEye("{2}{R}"));

        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED, new MayEffect(
                SequenceEffect.of(
                        new TargetPlayerGainsControlOfSourcePermanentUntilEndOfTurnEffect(),
                        new UntapPermanentsEffect(TapUntapScope.SOURCE_PERMANENT),
                        new GoadCreaturesUntilNextTurnEffect(new PermanentIsSourcePermanentPredicate()),
                        new GrantStaticEffectToSourceUntilEndOfTurnEffect(new CantBeSacrificedEffect())),
                "Have that player gain control of Slicer until end of turn?",
                new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "SlicerHighSpeedAntagonist";
    }
}
