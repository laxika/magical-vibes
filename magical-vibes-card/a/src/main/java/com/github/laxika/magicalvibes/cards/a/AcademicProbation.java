package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CantPlayChosenNameUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "7")
public class AcademicProbation extends Card {

    public AcademicProbation() {
        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Choose a nonland card name. Until your next turn, opponents can't cast spells with the chosen name",
                        SequenceEffect.of(ChooseCardNameAtResolutionEffect.excluding(CardType.LAND),
                                CantPlayChosenNameUntilNextTurnEffect.opponentsCantCast())),
                new ChooseOneEffect.ChooseOneOption(
                        "Choose target nonland permanent. Until your next turn, it can't attack or block, and its activated abilities can't be activated",
                        new LockTargetPermanentEffect(
                                true, true, true, Duration.UNTIL_YOUR_NEXT_TURN, TargetPredicates.permanent()),
                        TargetFilters.nonlandPermanent())
        )));
    }
}
