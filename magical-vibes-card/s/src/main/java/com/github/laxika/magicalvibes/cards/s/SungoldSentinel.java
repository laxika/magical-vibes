package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseColorAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ClearChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantControllerAndPermanentsHexproofFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasChosenSpellColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "37")
@CardRegistration(set = "DBL", collectorNumber = "37")
public class SungoldSentinel extends Card {

    public SungoldSentinel() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), ExileGraveyardCardsEffect.upToOneTargetFromAnyGraveyard());
        addTrigger(Triggers.attacks(), ExileGraveyardCardsEffect.upToOneTargetFromAnyGraveyard());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(
                        new ChooseColorAtResolutionEffect(),
                        GrantControllerAndPermanentsHexproofFromColorsEffect.chosenColor(Subjects.source()),
                        new MakeCreatureBlockableOnlyByFilterThisTurnEffect(
                                new PermanentNotPredicate(new PermanentHasChosenSpellColorPredicate()),
                                "creatures that aren't the chosen color", true),
                        new ClearChosenColorEffect()),
                "Coven — {1}{W}: Choose a color. This creature gains hexproof from that color until end of turn "
                        + "and can't be blocked by creatures of that color this turn. Activate only if you control "
                        + "three or more creatures with different powers.",
                ActivationTimingRestriction.COVEN));
    }
}
