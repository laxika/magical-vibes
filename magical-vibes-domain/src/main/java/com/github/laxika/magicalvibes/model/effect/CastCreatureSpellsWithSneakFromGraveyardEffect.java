package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CastingCost;
import com.github.laxika.magicalvibes.model.ReturnPermanentsCost;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.DuringYourDeclareBlockers;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsUnblockedAttackingPredicate;

import java.util.List;

/**
 * Static permission to cast matching creature spells from the controller's graveyard using sneak.
 */
public record CastCreatureSpellsWithSneakFromGraveyardEffect(CardPredicate filter, String sneakCost)
        implements CastSpellsFromGraveyardPermission {

    public CastCreatureSpellsWithSneakFromGraveyardEffect(String sneakCost) {
        this(new CardTypePredicate(CardType.CREATURE), sneakCost);
    }

    @Override
    public List<CastingCost> additionalCosts() {
        return List.of(new ReturnPermanentsCost(1, new PermanentIsUnblockedAttackingPredicate()));
    }

    @Override
    public String alternateManaCost() {
        return sneakCost;
    }

    @Override
    public boolean sneak() {
        return true;
    }

    @Override
    public Condition availabilityCondition() {
        return new DuringYourDeclareBlockers();
    }
}
