package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "M15", collectorNumber = "75")
@CardRegistration(set = "C17", collectorNumber = "90")
public class PolymorphistsJest extends Card {

    public PolymorphistsJest() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addSpellEffect(new LosesAllAbilitiesEffect(GrantScope.TARGET_PLAYERS_CREATURES,
                                EffectDuration.UNTIL_END_OF_TURN))
                .addSpellEffect(new GrantColorUntilEndOfTurnEffect(CardColor.BLUE, GrantScope.TARGET_PLAYERS_CREATURES))
                .addSpellEffect(new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.FROG,
                                GrantScope.TARGET_PLAYERS_CREATURES))
                .addSpellEffect(new SetBasePowerToughnessEffect(1, 1, GrantScope.TARGET_PLAYERS_CREATURES));
    }
}
