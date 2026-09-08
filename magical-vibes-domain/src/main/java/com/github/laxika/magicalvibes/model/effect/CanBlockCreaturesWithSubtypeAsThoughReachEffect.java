package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Static effect: this creature can block creatures with the given subtype as though it had reach. */
public record CanBlockCreaturesWithSubtypeAsThoughReachEffect(CardSubtype subtype)
        implements BlockabilityPermissionEffect {

    @Override
    public PermanentPredicate blocksAsThoughReachForAttackers() {
        return new PermanentHasSubtypePredicate(subtype);
    }
}
