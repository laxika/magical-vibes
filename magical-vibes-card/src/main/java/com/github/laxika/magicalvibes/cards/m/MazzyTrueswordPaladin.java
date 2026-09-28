package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringAuraFromGraveyardMayCastUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingOpponentOfSourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "241")
@CardRegistration(set = "HBG", collectorNumber = "283")
public class MazzyTrueswordPaladin extends Card {

    public MazzyTrueswordPaladin() {
        addEffect(EffectSlot.ON_ANY_CREATURE_ATTACKS, new TriggeringPermanentConditionalEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsEnchantedPredicate(),
                        new PermanentIsAttackingOpponentOfSourceControllerPredicate())),
                SequenceEffect.of(
                        new BoostTargetCreatureEffect(2, 0),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET))));

        addEffect(EffectSlot.ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new ExileTriggeringAuraFromGraveyardMayCastUntilNextTurnEffect());
    }
}
