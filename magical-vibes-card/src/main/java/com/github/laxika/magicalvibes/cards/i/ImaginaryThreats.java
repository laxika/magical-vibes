package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TargetPlayersCreaturesMustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;


@CardRegistration(set = "HOU", collectorNumber = "37")
public class ImaginaryThreats extends Card {

    public ImaginaryThreats() {
        // Creatures target opponent controls attack this turn if able.
        // During that player's next untap step, creatures they control don't untap.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.SPELL, new TargetPlayersCreaturesMustAttackThisTurnEffect())
                .addEffect(EffectSlot.SPELL, new SkipNextUntapEffect(
                        TapUntapScope.TARGET_PLAYERS_PERMANENTS, new PermanentIsCreaturePredicate()));

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");
    }
}
