package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "ECL", collectorNumber = "12")
public class CuriousColossus extends Card {

    public CuriousColossus() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new LosesAllAbilitiesEffect(GrantScope.TARGET_PLAYERS_CREATURES,
                                EffectDuration.UNTIL_END_OF_TURN))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GrantSubtypeUntilEndOfTurnEffect(CardSubtype.COWARD,
                                GrantScope.TARGET_PLAYERS_CREATURES))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new SetBasePowerToughnessEffect(1, 1, GrantScope.TARGET_PLAYERS_CREATURES));
    }
}
