package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/**
 * Exiles a target permanent and either returns it tapped under the effect controller's control
 * when that player controlled it as the effect began resolving, or creates a token for the
 * permanent's controller otherwise.
 *
 * @param tokenForOtherController the token created for a permanent controlled by another player
 */
public record ExileTargetPermanentAndReturnIfControlledOtherwiseCreateTokenEffect(
        CreateTokenEffect tokenForOtherController
) implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(),
                new PermanentNotPredicate(new PermanentIsLandPredicate()));
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
