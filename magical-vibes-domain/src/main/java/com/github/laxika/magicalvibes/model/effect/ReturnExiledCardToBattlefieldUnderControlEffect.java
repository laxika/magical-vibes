package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Returns a specific exiled card to the battlefield under the resolving ability's controller. */
public record ReturnExiledCardToBattlefieldUnderControlEffect(UUID exiledCardId) implements CardEffect {
}
