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
 * What a player-damage replacement needs to know about the damage it may modify, read from the
 * game at the moment it is asked.
 *
 * @param sourcePermanent    the damage source while it is on the battlefield, otherwise {@code null}
 * @param sourceControllerId the source's controller, or the stack entry's controller
 * @param damageSourceColors the source's colors for damage purposes (after Ghostly Flame)
 * @param damageSourceId     the identity prevention and redirection shields key the source by
 */
public record PlayerDamageContext(GameData gameData, DamageEvent event, Card source, Permanent sourcePermanent,
                                  UUID sourceControllerId, Set<CardColor> damageSourceColors,
                                  UUID damageSourceId) implements DamageCandidateSource.DamageContext {

    public StackEntry entry() {
        return event.entry();
    }

    public UUID playerId() {
        return event.recipientPlayerId();
    }

    public String playerName() {
        return gameData.playerIdToName.get(playerId());
    }
}
