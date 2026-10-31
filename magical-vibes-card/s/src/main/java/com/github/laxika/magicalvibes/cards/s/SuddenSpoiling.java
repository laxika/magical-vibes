package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "TSP", collectorNumber = "135")
@CardRegistration(set = "TSR", collectorNumber = "144")
@CardRegistration(set = "C13", collectorNumber = "94")
@CardRegistration(set = "C14", collectorNumber = "164")
public class SuddenSpoiling extends Card {

    public SuddenSpoiling() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addSpellEffect(new LosesAllAbilitiesEffect(GrantScope.TARGET_PLAYERS_CREATURES,
                                EffectDuration.UNTIL_END_OF_TURN))
                .addSpellEffect(new SetBasePowerToughnessEffect(0, 2, GrantScope.TARGET_PLAYERS_CREATURES));
    }
}
