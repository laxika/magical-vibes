package com.github.laxika.magicalvibes.service.effect.replacement.damage;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.replacement.DamageEvent;

import java.util.Set;
import java.util.UUID;

/**
 * What a permanent-damage replacement needs to know about the damage it may modify, read from the
 * game at the moment it is asked.
 *
 * @param target             the damaged permanent
 * @param targetControllerId its controller
 * @param damageSource       the permanent dealing the damage: the fight or bite source, otherwise
 *                           the stack entry's source while it is on the battlefield
 * @param sourceControllerId the damage source's controller, or the stack entry's controller
 * @param sourceCard         the card dealing the damage
 * @param sourcePermanentId  the source permanent's id for source-keyed shields, or {@code null} for a spell
 * @param damageSourceKey    the identity source-keyed shields use: the source permanent, or the spell's card
 * @param damageSourceColors the source's colors for damage purposes (after Ghostly Flame)
 */
public record PermanentDamageContext(GameData gameData, DamageEvent event, Permanent target,
                                     UUID targetControllerId, Permanent damageSource, UUID sourceControllerId,
                                     Card sourceCard, UUID sourcePermanentId, UUID damageSourceKey,
                                     Set<CardColor> damageSourceColors)
        implements DamageCandidateSource.DamageContext {

    public StackEntry entry() {
        return event.entry();
    }

    /** The damage is dealt by the spell or ability itself rather than by a fighting or biting permanent. */
    public boolean dealtByStackEntry() {
        return event.explicitSource() == null;
    }

    /** "Damage can't be prevented or redirected" applies to the damaged permanent this turn. */
    public boolean targetProtectedFromPrevention() {
        return target.isDamageCantBePreventedOrRedirectedThisTurn();
    }
}
