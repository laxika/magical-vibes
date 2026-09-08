package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Puts a specific exiled card onto the battlefield under the resolving effect's controller. */
public record PutExiledCardOntoBattlefieldUnderControllerEffect(UUID exiledCardId) implements CardEffect {
}
