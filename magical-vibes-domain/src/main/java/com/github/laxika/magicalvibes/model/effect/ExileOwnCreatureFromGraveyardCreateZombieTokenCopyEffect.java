package com.github.laxika.magicalvibes.model.effect;

/**
 * Non-targeting "you may exile a creature card from your graveyard. If you do, create a token that's
 * a copy of that card, except it's a 4/4 black Zombie. It gains haste until end of turn."
 *
 * <p>Choose up to one creature card from the controller's graveyard as the ability resolves
 * (God-Pharaoh's Gift — does not target). Declining / choosing none creates no token. The token's
 * creature types become exactly Zombie (not in addition), its color becomes black, and base P/T
 * become 4/4; haste is a non-copiable until-end-of-turn grant.
 */
public record ExileOwnCreatureFromGraveyardCreateZombieTokenCopyEffect() implements CardEffect {
}
