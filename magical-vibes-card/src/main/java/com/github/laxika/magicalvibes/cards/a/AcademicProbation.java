package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOpponentsCantCastUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "7")
public class AcademicProbation extends Card {

    public AcademicProbation() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Choose a nonland card name. Until your next turn, opponents can't cast spells with the chosen name",
                        new ChooseCardNameOpponentsCantCastUntilNextTurnEffect(List.of(CardType.LAND))),
                new ChooseOneEffect.ChooseOneOption(
                        "Choose target nonland permanent. Until your next turn, it can't attack or block, and its activated abilities can't be activated",
                        new LockTargetPermanentEffect(
                                true, true, true, EffectDuration.UNTIL_YOUR_NEXT_TURN, TargetPredicates.permanent()),
                        TargetFilters.nonlandPermanent())
        )));
    }
}
