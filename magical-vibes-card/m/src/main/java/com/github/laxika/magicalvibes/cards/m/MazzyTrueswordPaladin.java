package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringAuraFromGraveyardMayCastUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingOpponentOfSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "241")
@CardRegistration(set = "HBG", collectorNumber = "283")
public class MazzyTrueswordPaladin extends Card {

    public MazzyTrueswordPaladin() {
        addTrigger(Triggers.creatureAttacks()
                        .withFilter(new PermanentAllOfPredicate(List.of(
                                new PermanentIsEnchantedPredicate(),
                                new PermanentIsAttackingOpponentOfSourceControllerPredicate()))),
                SequenceEffect.of(
                        new BoostTargetCreatureEffect(2, 0),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)));

        addTrigger(Triggers.dies(Triggers.AURA_OR_EQUIPMENT, ObjectRelation.YOU_CONTROL),
                new ExileTriggeringAuraFromGraveyardMayCastUntilNextTurnEffect());
    }
}
