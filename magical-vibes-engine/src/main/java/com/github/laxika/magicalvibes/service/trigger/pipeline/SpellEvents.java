package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCopyRider;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.EmpowerNextCreatureSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.VisualCueType;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.rules.SpellEvent;
import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.duration.DurationClock;
import com.github.laxika.magicalvibes.service.event.VisualCueService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The emitters of a spell becoming cast and an ability being activated ({@link SpellEvent.Cast},
 * {@link SpellEvent.Activated}): the code that casts a spell or activates an ability calls one once the spell or ability
 * is on the stack and its costs are paid (CR 601.2i, 602.2b). The emitter keeps the game's bookkeeping of the cast or
 * activation, takes what its payment leaves behind for its abilities (the copy costs paid, the mana spent that copies
 * it) into the event, and emits the event as an occurrence of its own, which {@link SpellTriggers} matches at once.
 * Every ability the event triggers, keyword and engine-made ones among them, is that matcher's.
 */
@Component
public class SpellEvents {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final TriggeredAbilityQueueService triggeredAbilityQueueService;
    private final ObjectProvider<VisualCueService> visualCueService;
    private final DurationClock durationClock;

    public SpellEvents(@Lazy GameQueryService gameQueryService,
                       @Lazy PredicateEvaluationService predicateEvaluationService,
                       @Lazy PlayerInputService playerInputService,
                       @Lazy TriggeredAbilityQueueService triggeredAbilityQueueService,
                       ObjectProvider<VisualCueService> visualCueService,
                       @Lazy DurationClock durationClock) {
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.playerInputService = playerInputService;
        this.triggeredAbilityQueueService = triggeredAbilityQueueService;
        this.visualCueService = visualCueService;
        this.durationClock = durationClock;
    }

    /** {@code spellCard} became cast by {@code castingPlayerId} from their hand. */
    public void cast(GameData gameData, Card spellCard, UUID castingPlayerId) {
        cast(gameData, spellCard, castingPlayerId, Zone.HAND);
    }

    /** {@code spellCard} became cast from its caster's hand, or else from a graveyard. */
    public void cast(GameData gameData, Card spellCard, UUID castingPlayerId, boolean castFromHand) {
        cast(gameData, spellCard, castingPlayerId, castFromHand ? Zone.HAND : Zone.GRAVEYARD);
    }

    /** {@code spellCard} became cast from {@code castZone}. */
    public void cast(GameData gameData, Card spellCard, UUID castingPlayerId, Zone castZone) {
        cast(gameData, spellCard, castingPlayerId, castZone, null);
    }

    /** {@code spellCard} became cast from {@code castZone}, from among the cards exiled with a permanent. */
    public void cast(GameData gameData, Card spellCard, UUID castingPlayerId, Zone castZone,
                     UUID exiledSourcePermanentId) {
        cast(gameData, spellCard, castingPlayerId, castZone, exiledSourcePermanentId, false);
    }

    /**
     * {@code spellCard} became cast (CR 601.2i) by {@code castingPlayerId} from {@code castZone} ({@code
     * exiledSourcePermanentId} the permanent it was exiled with, if any), face down when {@code castFaceDown}. The cast
     * is recorded first (the turn's casts by zone), so the abilities the event triggers see it among the turn's
     * spells, and the effects that last until such a spell is cast end as the event is matched; the records of its payment are cleared once they
     * have triggered.
     */
    public void cast(GameData gameData, Card spellCard, UUID castingPlayerId, Zone castZone,
                     UUID exiledSourcePermanentId, boolean castFaceDown) {
        applyNextSpellRiders(gameData, spellCard, castingPlayerId, castFaceDown);
        emitSpellCastCue(gameData, spellCard);
        gameData.initializeCardIntensity(spellCard);
        Zone copyCreatedIn = gameData.castCopyCreationZones.remove(spellCard.getId());
        if (copyCreatedIn != null) {
            // A copy of a card waits in exile to be cast, but it was created in its card's zone and is cast from
            // there (CR 707.12).
            castZone = copyCreatedIn;
        }
        StackEntry spellEntry = null;
        for (StackEntry spell : gameData.stack) {
            if (copyCreatedIn != null && spell.isCopy() && spell.getCard() != null
                    && spell.getCard().getId().equals(spellCard.getId())) {
                spell.setSourceZone(copyCreatedIn);
            }
            if (spell.getCard() != null && spell.getCard().getId().equals(spellCard.getId()) && !spell.isCopy()) {
                spell.setAdditionalEnterCounters(gameData.spellAdditionalEnterCounters
                        .getOrDefault(spellCard.getId(), 0));
                // The mana spent to cast it, and the part of it creatures produced, as the spell becomes cast (CR
                // 601.2i): the game's records are cleared once its cast triggers are collected, so the cast triggers
                // read them from the spell's entry (or their snapshot of it) as they resolve.
                spell.setManaSpentToCast(gameData.getSpellCastManaSpent(spellCard.getId()));
                spell.setCreatureManaSpentToCast(gameData.getSpellCastCreatureManaSpent(spellCard.getId()));
                spell.setManaSpentByColorToCast(gameData.spellCastManaSpentByColor.get(spellCard.getId()));
                spellEntry = spell;
            }
        }
        gameData.recordSpellCastFromZone(castingPlayerId, castZone, spellCard);
        if (castZone == Zone.EXILE && !spellCard.isToken()) {
            gameData.recordCardPlayedFromExile(castingPlayerId);
        }
        if (castZone == Zone.HAND) {
            gameData.recordSpellCastFromHand(spellCard);
        } else {
            gameData.recordSpellCastFromOutsideHand(castingPlayerId);
            gameData.playersWhoPlayedOrCastFromOutsideHandThisTurn.add(castingPlayerId);
        }

        boolean firstSpellOfGame = gameData.playersWhoCastFirstSpellInGame.add(castingPlayerId);
        Integer conspireCopies = gameData.conspiredSpellCopyCounts.remove(spellCard.getId());
        Integer casualtyCopies = gameData.casualtySpellCopyCounts.remove(spellCard.getId());
        int instantOrSorceryManaCopies = 0;
        int spellOrAbilityManaCopies = 0;
        ManaPool pool = gameData.playerManaPools.get(castingPlayerId);
        if (pool != null) {
            boolean instantOrSorcery = spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY);
            boolean red = gameQueryService.getEffectiveCardColors(gameData, spellCard).contains(CardColor.RED);
            int instantOrSorceryCopies = pool.takeSpentCopyGrantingMana(ManaCopyRider.INSTANT_OR_SORCERY_SPELL);
            int redInstantOrSorceryCopies = pool.takeSpentCopyGrantingMana(ManaCopyRider.RED_INSTANT_OR_SORCERY_SPELL);
            if (instantOrSorcery) {
                instantOrSorceryManaCopies += instantOrSorceryCopies + (red ? redInstantOrSorceryCopies : 0);
            }
            spellOrAbilityManaCopies = pool.takeSpentCopyGrantingMana(ManaCopyRider.SPELL_OR_ABILITY);
        }
        boolean onStack = spellEntry != null;

        SpellEvent.Cast castEvent = new SpellEvent.Cast(spellCard, castingPlayerId, castZone, exiledSourcePermanentId,
                castFaceDown, firstSpellOfGame, onStack && conspireCopies != null ? conspireCopies : 0,
                onStack && casualtyCopies != null ? casualtyCopies : 0, onStack ? instantOrSorceryManaCopies : 0,
                onStack ? spellOrAbilityManaCopies : 0);
        gameData.rulesEvents.separately(() -> gameData.rulesEvents.emit(castEvent));

        gameData.clearSpellCastManaSpent(spellCard.getId());
        gameData.clearSpellCastManaSources(spellCard.getId());
        gameData.clearSpellCastTreasureMana(spellCard.getId());
        gameData.clearSpellCastCreatureManaSpent(spellCard.getId());

        playerInputService.processNextMayAbility(gameData);
    }

    /**
     * Applies the "next [matching] spell you cast this turn" riders ({@link EmpowerNextCreatureSpellThisTurnEffect}: it
     * can't be countered, its permanent enters with additional +1/+1 counters) to {@code spellCard} as it becomes cast
     * (CR 601.2i), before its entry takes its entering counters. A rider applies when the duration clock's judge says
     * this cast is the one that ends it, so the spell that uses a rider up is the spell it applies to.
     */
    private void applyNextSpellRiders(GameData gameData, Card spellCard, UUID castingPlayerId, boolean castFaceDown) {
        for (FloatingContinuousEffect rider
                : gameData.floatingEffectsOnPlayer(castingPlayerId, EmpowerNextCreatureSpellThisTurnEffect.class)) {
            if (!durationClock.waitsForSpell(gameData, rider, spellCard, castingPlayerId, castFaceDown)) {
                continue;
            }
            var grant = (EmpowerNextCreatureSpellThisTurnEffect) rider.effect();
            if (grant.uncounterable()) {
                gameData.spellsMadeUncounterable.add(spellCard.getId());
            }
            if (grant.additionalPlusOneCounters() > 0) {
                gameData.spellAdditionalEnterCounters.merge(spellCard.getId(), grant.additionalPlusOneCounters(),
                        Integer::sum);
            }
        }
    }

    /**
     * One activation of an activated ability, as it is once the ability is on the stack and its costs are paid, which
     * is when "whenever [a player] activates an ability" abilities trigger (CR 602.2b, CR 601.2i).
     *
     * @param activatingPlayerId             the player who activated the ability
     * @param abilityEntry                   the activated ability on the stack, with its targets and X; for a mana
     *                                       ability, which doesn't use the stack, the entry it would have been
     * @param ability                        the activated ability
     * @param activatedPermanent             the permanent whose ability it is, as it last existed when a cost removed
     *                                       it, or {@code null} for an ability of a card that isn't a permanent (in a
     *                                       hand, a graveyard, exile or the command zone; CR 109.2)
     * @param activatedPermanentControllerId that permanent's controller as the activation began; the activating
     *                                       player for an ability of a card in a hand or a graveyard
     * @param manaAbility                    whether it is a mana ability (CR 605.1a)
     */
    public record Activation(UUID activatingPlayerId, StackEntry abilityEntry, ActivatedAbility ability,
                             Permanent activatedPermanent, UUID activatedPermanentControllerId,
                             boolean manaAbility) {}

    /**
     * An activated ability was activated: it is on the stack and its costs are paid (CR 602.2b, 601.2i). The mana spent
     * that copies the ability it was spent on is taken from the pool ({@link SpellEvent.Activated#manaLinkedCopies}; a
     * mana ability can't be copied, so for one it does nothing), and the activation is the rules event the "whenever [a
     * player] activates an ability" abilities, delayed ones among them, trigger on, with the life paid to activate it;
     * then the abilities that choose their targets as they go on the stack (CR 603.3d) choose them, above the activated
     * ability.
     */
    public void activated(GameData gameData, Activation activation) {
        ManaPool pool = gameData.playerManaPools.get(activation.activatingPlayerId());
        int manaLinkedCopies = pool == null || activation.manaAbility() ? 0
                : pool.takeSpentCopyGrantingMana(ManaCopyRider.SPELL_OR_ABILITY);
        if (pool != null) {
            pool.discardSpentCopyGrantingMana();
        }
        if (activation.abilityEntry() == null) {
            return;
        }
        int lifePaid = gameData.activationLifePaid;
        if (!activation.manaAbility()) {
            gameData.trackingActivationLifePaid = false;
            gameData.activationLifePaid = 0;
        }
        boolean treasureMana = gameData.abilityActivationUsedTreasureMana
                .getOrDefault(activation.abilityEntry().getCard().getId(), false);
        SpellEvent.Activated event = new SpellEvent.Activated(activation.activatingPlayerId(),
                activation.abilityEntry(), activation.ability(), activation.activatedPermanent(),
                activation.activatedPermanentControllerId(), activation.manaAbility(), lifePaid, treasureMana,
                manaLinkedCopies);
        gameData.rulesEvents.separately(() -> gameData.rulesEvents.emit(event));
        // An ability that chooses its targets as it goes on the stack (CR 603.3d) chooses them now, above the
        // activated ability; a mana ability's wait with it.
        if (!activation.manaAbility()) {
            triggeredAbilityQueueService.beginStackDoorChoices(gameData);
        }
    }

    /**
     * Emits the cosmetic {@link VisualCueType#SPELL_CAST} cue for a spell that was just put on the stack. Targets are
     * public once chosen, so the cue names the first player, the first permanent and the first spell the spell
     * targets; a face-down spell keeps its name and colors hidden.
     */
    private void emitSpellCastCue(GameData gameData, Card spellCard) {
        VisualCueService cues = visualCueService == null ? null : visualCueService.getIfAvailable();
        if (cues == null) {
            return;
        }
        StackEntry spell = null;
        for (StackEntry entry : gameData.stack) {
            if (entry.getCard() != null && entry.getCard().getId().equals(spellCard.getId()) && !entry.isCopy()) {
                spell = entry;
            }
        }
        if (spell == null) {
            return;
        }
        List<UUID> targetIds = new ArrayList<>();
        if (spell.getTargetId() != null) {
            targetIds.add(spell.getTargetId());
        }
        if (spell.getTargetIds() != null) {
            targetIds.addAll(spell.getTargetIds());
        }
        UUID targetPlayerId = null;
        UUID targetPermanentId = null;
        UUID targetCardId = null;
        for (UUID targetId : targetIds) {
            if (gameData.playerIds.contains(targetId)) {
                targetPlayerId = targetPlayerId != null ? targetPlayerId : targetId;
            } else if (gameQueryService.findPermanentById(gameData, targetId) != null) {
                targetPermanentId = targetPermanentId != null ? targetPermanentId : targetId;
            } else if (targetCardId == null) {
                targetCardId = targetedSpellCardId(gameData, spell, targetId);
            }
        }
        cues.emit(gameData, VisualCueType.SPELL_CAST, VisualCueService.Source.of(spell),
                targetPermanentId, targetPlayerId, targetCardId, null, GameEventAudience.allPlayers());
    }

    private static UUID targetedSpellCardId(GameData gameData, StackEntry spell, UUID targetId) {
        for (StackEntry entry : gameData.stack) {
            if (entry != spell && entry.getCard() != null && entry.getTargetableId().equals(targetId)
                    && entry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                    && entry.getEntryType() != StackEntryType.TRIGGERED_ABILITY) {
                return entry.getCard().getId();
            }
        }
        return null;
    }
}
