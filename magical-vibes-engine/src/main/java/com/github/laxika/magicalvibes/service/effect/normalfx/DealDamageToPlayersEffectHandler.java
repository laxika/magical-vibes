package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.DamageSource;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Deals damage to the players a {@link DealDamageToPlayersEffect}'s {@link PlayerSubject} names, found through
 * {@link SubjectResolutionService} (several in APNAP order), one {@link DamageSupport#dealDamageToPlayer} call per
 * player, then checks the win condition once. The stack entry's controller id is never remapped, so opponent-source
 * damage-reduction shields keep applying (the source stays the caster). With {@code DamageSource.EVENT_DAMAGE_SOURCE}
 * the damage is dealt through {@link DamageSupport#entryDealtByEventDamageSource}, whose source is the permanent that
 * dealt the trigger event's damage; with {@code DamageSource.TRIGGERING_PERMANENT} through
 * {@link DamageSupport#entryDealtByTriggeringPermanent}, whose source is the permanent the trigger event was about; the
 * players and the amount are still read from the resolving entry.
 *
 * <p>The amount is evaluated for each player dealt damage: with that player as the controller for
 * {@code eachPlayer()}, as the target for {@code defendingPlayer()}, else in the resolving entry's own context.
 * "You" and "that creature's controller" check only the stack entry's own prevention; the other subjects also check
 * prevention of instant and sorcery damage and of a targeted spell's damage.
 */
@Component
@RequiredArgsConstructor
public class DealDamageToPlayersEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final PlayerInputService playerInputService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToPlayersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // The damage this step deals is added to its result as each damage event is dealt (DamageSupport).
        entry.recordStep(StepKind.DEAL_DAMAGE, StepOutcome.NONE);
        dealDamage(gameData, entry, (DealDamageToPlayersEffect) effect);
    }

    private void dealDamage(GameData gameData, StackEntry entry, DealDamageToPlayersEffect e) {
        // The entry the damage is dealt through: the resolving one, or one whose source is the permanent that dealt
        // the trigger event's damage ("that Archer deals that much damage"); the players are always read from the
        // resolving entry.
        StackEntry dealing = switch (e.damageSource()) {
            case EVENT_DAMAGE_SOURCE -> damageSupport.entryDealtByEventDamageSource(gameData, entry);
            case TRIGGERING_PERMANENT -> damageSupport.entryDealtByTriggeringPermanent(gameData, entry);
            case ABILITY_SOURCE -> entry;
        };
        if (dealing == null) {
            return;
        }
        UUID damageSourceId = dealing.getSourcePermanentId() != null
                ? dealing.getSourcePermanentId() : dealing.getCard().getId();
        int damageBefore = gameData.damageDealtThisTurnBySource.getOrDefault(damageSourceId, 0);
        boolean previousUnpreventable = gameData.unpreventableDamageInProgress;
        gameData.unpreventableDamageInProgress = previousUnpreventable || e.unpreventable();
        try {
            if (e.playerOrTheirPlaneswalker()) {
                resolvePlayerOrTheirPlaneswalker(gameData, entry, dealing, e);
            } else {
                resolvePlayers(gameData, entry, dealing, e);
            }
            if (dealing != entry) {
                // Dealt through the permanent that deals it: the damage is this step's.
                entry.addToStep(StepKind.DEAL_DAMAGE, dealing.getResolutionResults().outcome(StepKind.DEAL_DAMAGE));
            }

            if (e.recordDamageDealt()) {
                entry.setEventValue(gameData.damageDealtThisTurnBySource.getOrDefault(damageSourceId, 0)
                        - damageBefore);
            }
            gameOutcomeService.checkWinCondition(gameData);
        } finally {
            gameData.unpreventableDamageInProgress = previousUnpreventable;
        }
    }

    private void resolvePlayers(GameData gameData, StackEntry entry, StackEntry dealing, DealDamageToPlayersEffect e) {
        List<UUID> players = subjectResolutionService.players(gameData, entry, e, e.player());
        if (players.isEmpty() || isPrevented(gameData, dealing, e, players.getFirst())) {
            return;
        }
        boolean spellController = readsTarget(e, PermanentSubject.TargetSpell.class);
        for (UUID playerId : players) {
            int amount = evaluateAmount(gameData, entry, e, playerId);
            if (spellController && amount <= 0) {
                continue;
            }
            damageSupport.dealDamageToPlayer(gameData, dealing, playerId, amount);
        }
    }

    /**
     * Whether the damage is prevented before it is dealt. "You" and "that creature's controller" check only the
     * entry's own prevention, with their own log line; every other subject checks every source-wide prevention.
     */
    private boolean isPrevented(GameData gameData, StackEntry dealing, DealDamageToPlayersEffect e, UUID playerId) {
        if (e.player() instanceof PlayerSubject.You) {
            if (gameQueryService.isDamageFromStackEntryPrevented(gameData, dealing)) {
                gameLogService.append(gameData,
                        GameLog.cardThen(dealing.getCard(), "'s damage to controller is prevented."));
                return true;
            }
            return false;
        }
        if (readsTarget(e, PermanentSubject.Target.class)) {
            if (gameQueryService.isDamageFromStackEntryPrevented(gameData, dealing)) {
                gameLogService.append(gameData, GameLog.text(FaceDownVisibility.sourceName(gameData, dealing) + "'s damage to "
                        + gameData.playerIdToName.get(playerId) + " is prevented."));
                return true;
            }
            return false;
        }
        return damageSupport.isDamageSourcePreventedWithLog(gameData, dealing);
    }

    private static boolean readsTarget(DealDamageToPlayersEffect e, Class<? extends PermanentSubject> kind) {
        return e.player() instanceof PlayerSubject.ControllerOf controllerOf && kind.isInstance(controllerOf.permanent());
    }

    /**
     * "That player or a planeswalker that player controls": the player is found once, then, if they control a
     * planeswalker, the controller chooses without targeting and the effect is resolved again with the answer.
     */
    private void resolvePlayerOrTheirPlaneswalker(GameData gameData, StackEntry entry, StackEntry dealing,
                                                  DealDamageToPlayersEffect effect) {
        UUID player = entry.getOpponentChosenTargetPlayerId();
        if (player == null) {
            player = subjectResolutionService.player(gameData, entry, effect, effect.player());
            if (player == null || !gameData.playerIds.contains(player)) return;
            entry.setOpponentChosenTargetPlayerId(player);
            UUID thatPlayer = player;
            List<UUID> planeswalkers = gameData.playerBattlefields.getOrDefault(thatPlayer, List.of()).stream()
                    .filter(permanent -> gameQueryService.isPlaneswalker(gameData, permanent))
                    .map(Permanent::getId).toList();
            if (!planeswalkers.isEmpty()) {
                entry.setTargetId(null);
                gameData.rerunCurrentEffectAfterInteraction = true;
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.RandomOpponentDamageChoice(entry.getCard(), entry.getControllerId()));
                playerInputService.beginAnyTargetChoice(gameData, entry.getControllerId(), planeswalkers,
                        List.of(thatPlayer), entry.getCard().getName()
                                + " ? Choose the enchanted player or a planeswalker they control.");
                return;
            }
        }
        gameData.rerunCurrentEffectAfterInteraction = false;
        UUID recipient = entry.getTargetId() == null ? player : entry.getTargetId();
        if (!damageSupport.isDamageSourcePreventedWithLog(gameData, dealing)) {
            int amount = evaluateAmount(gameData, entry, effect, player);
            damageSupport.resolveAnyTargetDamage(gameData, dealing, recipient, amount, false);
        }
    }

    private int evaluateAmount(GameData gameData, StackEntry entry, DealDamageToPlayersEffect e, UUID playerId) {
        // Curse of Thirst: damage equals the number of matching permanents attached to the player.
        if (e.attachedCountFilter() != null) {
            return damageSupport.countPermanentsAttachedToPlayer(gameData, playerId, e.attachedCountFilter());
        }
        // Source-relative amounts use the live source permanent when it is still on the battlefield,
        // else the last-known snapshot (e.g. sacrificed as an activation cost).
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext context = AmountContext.forStackEntry(entry, source);
        if (e.player() instanceof PlayerSubject.Each each && each.relation() == PlayerSubject.Relation.ALL) {
            context = context.withControllerId(playerId);
        } else if (e.player() instanceof PlayerSubject.DefendingPlayer) {
            context = context.withTargetPermanentId(playerId);
        }
        return amountEvaluationService.evaluate(gameData, e.amount(), context);
    }
}
