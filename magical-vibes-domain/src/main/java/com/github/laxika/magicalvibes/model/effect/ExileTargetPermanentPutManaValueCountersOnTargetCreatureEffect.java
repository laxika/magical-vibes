package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

/**
 * Exiles the first targeted artifact or enchantment, then puts that permanent's mana value in
 * +1/+1 counters on the second targeted creature.
 *
 * <p>The targets are independent: if the permanent target is illegal, nothing is exiled and no
 * counters are placed; if the creature target is illegal, the artifact or enchantment is still
 * exiled.</p>
 */
public record ExileTargetPermanentPutManaValueCountersOnTargetCreatureEffect(
        int permanentTargetGroup, int creatureTargetGroup) implements RemovalEffect {

    public ExileTargetPermanentPutManaValueCountersOnTargetCreatureEffect() {
        this(0, 1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(), new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsEnchantmentPredicate())));
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
