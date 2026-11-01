package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Lists the triggered abilities a rules event may trigger, as the layers leave them (CR 613.1f).
 *
 * <p>For now it lists only the abilities of the object that moved, the candidates of a {@code THIS} trigger:
 * <ul>
 *   <li>A permanent that left the battlefield triggers as it last existed there (CR 603.10a): the abilities of
 *       the effective card it left as (also the source the stack entry names), those granted to it until it
 *       leaves, and those other objects' static abilities granted it, all on the event's snapshot. A permanent
 *       that had lost all its abilities has none of those, and a face-down one has none of its card's. Abilities
 *       granted until end of turn count either way, as the slot collectors count them. Its delayed "when this
 *       dies" triggers aren't abilities of it, so they count either way too; until their family moves they ride
 *       on the snapshot as ON_DEATH effects.</li>
 *   <li>Every moved card, a departed permanent's included, also triggers as it exists after the move, in the
 *       zone it went to (CR 603.10): its card's abilities and its perpetual grants. That is where "when this is
 *       put into a graveyard from anywhere" triggers from, even for a move from the battlefield (CR 603.6c).
 *       Cards in graveyards have none while an effect such as Yixlid Jailer's says so.</li>
 * </ul>
 * A candidate's zone says which of the two it is, so each trigger condition matches only the one it functions
 * from.
 * Abilities its owner perpetually lost are left out. Step 11 adds the other objects' abilities (the
 * battlefield's, with the look-back of a simultaneous batch), which the other {@code ObjectRelation}s need.
 */
@Service
public class TriggerIndex {

    private final GameQueryService gameQueryService;

    public TriggerIndex(@Lazy GameQueryService gameQueryService) {
        this.gameQueryService = gameQueryService;
    }

    /** The triggered abilities of the object that moved in {@code event}. */
    public List<TriggerCandidate> movingObjectCandidates(GameData gameData, ZoneChangeEvent event) {
        List<TriggerCandidate> candidates = new ArrayList<>();
        if (event.leftBattlefield()) {
            departedPermanentCandidates(gameData, event, candidates);
        }
        arrivedCardCandidates(gameData, event, candidates);
        return candidates;
    }

    private void arrivedCardCandidates(GameData gameData, ZoneChangeEvent event, List<TriggerCandidate> into) {
        Card card = event.card();
        Zone zone = event.to();
        if (zone == Zone.GRAVEYARD && gameQueryService.graveyardCardsHaveLostAllAbilities(gameData)) {
            return;
        }
        List<TriggeredAbility> abilities = new ArrayList<>(withoutLost(gameData, card.getId(),
                card.getTriggeredAbilities()));
        triggered(gameData.perpetualAbilities(card.getId()), abilities);
        UUID controllerId = event.ownerId() != null ? event.ownerId() : gameData.ownerOf(card);
        for (TriggeredAbility ability : abilities) {
            into.add(new TriggerCandidate(card, null, controllerId, zone, ability));
        }
    }

    private void departedPermanentCandidates(GameData gameData, ZoneChangeEvent event, List<TriggerCandidate> into) {
        Permanent lastKnown = event.permanent();
        if (lastKnown == null) {
            return;
        }
        Card card = lastKnown.getCard();
        List<TriggeredAbility> abilities = new ArrayList<>();
        if (!lastKnown.isLosesAllAbilitiesUntilEndOfTurn()) {
            if (!lastKnown.isFaceDown()) {
                abilities.addAll(withoutLost(gameData, card.getId(), card.getTriggeredAbilities()));
            }
            abilities.addAll(lastKnown.getPersistentTriggeredAbilities());
            triggered(event.grantedAbilities(), abilities);
        }
        // An ability granted until end of turn has a later timestamp than most "loses all abilities" effects
        // (Infuse with Vitality after Humble), so, as the slot collectors do, it stays.
        abilities.addAll(lastKnown.getTemporaryTriggeredAbilities());
        for (CardEffect delayed : lastKnown.getDelayedDeathEffects()) {
            abilities.add((TriggeredAbility) LegacySlots.ability(EffectSlot.ON_DEATH, delayed));
        }
        for (TriggeredAbility ability : abilities) {
            into.add(new TriggerCandidate(card, lastKnown, event.controllerId(), Zone.BATTLEFIELD, ability));
        }
    }

    private static void triggered(List<? extends Ability> abilities, List<TriggeredAbility> into) {
        for (Ability ability : abilities) {
            if (ability instanceof TriggeredAbility triggered) {
                into.add(triggered);
            }
        }
    }

    /** {@code abilities} without the effects the card perpetually lost; an ability left with no effect is gone. */
    private static List<TriggeredAbility> withoutLost(GameData gameData, UUID cardId,
                                                      List<TriggeredAbility> abilities) {
        Set<CardEffect> removed = gameData.perpetuallyRemovedTriggeredAbilities.get(cardId);
        if (removed == null || removed.isEmpty()) {
            return abilities;
        }
        List<TriggeredAbility> kept = new ArrayList<>(abilities.size());
        for (TriggeredAbility ability : abilities) {
            List<EffectRegistration> effects = ability.effects().stream()
                    .filter(registration -> !removed.contains(registration.effect()))
                    .toList();
            if (effects.size() == ability.effects().size()) {
                kept.add(ability);
            } else if (!effects.isEmpty() && !effects.getFirst().simultaneousWithPrevious()) {
                kept.add(ability.withEffects(effects));
            }
        }
        return kept;
    }
}
