package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LastKnownCardStats;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ExileTargetPermanentEffect}. A plain {@code target()} keeps the target path: every chosen target of
 * the effect's group, its controller and last-known card, power and toughness recorded for later steps, and the token for its
 * controller. "That creature" ({@code triggering()}) takes the same path. Every other subject ("this permanent",
 * "enchanted creature") is found by {@link SubjectResolutionService}; a permanent that already left is not exiled.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final DestructionSupport destructionSupport;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exile = (ExileTargetPermanentEffect) effect;
        PermanentSubject subject = exile.subject();
        if (!(subject instanceof PermanentSubject.Triggering)
                && !(subject instanceof PermanentSubject.Target target && target.group() == null)) {
            exileSubject(gameData, entry, exile);
            return;
        }
        List<UUID> exiled = new java.util.ArrayList<>();
        List<UUID> exiledControllers = new java.util.ArrayList<>();
        // Multi-target / optional group: exile each chosen target for this effect's group
        // (targetsForEffect). An empty group ("any number" / "up to N" with none chosen) exiles
        // nothing — do not fall back to List.of(null) when targetId is unset.
        // "Exile that creature" (triggering()): the triggering permanent, if it is still on the battlefield.
        boolean triggering = subject instanceof PermanentSubject.Triggering;
        List<UUID> targetIds = triggering
                ? java.util.Collections.singletonList(entry.getTriggeringPermanentId())
                : entry.targetsForEffect(effect);
        if (triggering) {
            targetIds = targetIds.get(0) == null ? List.of() : targetIds;
        } else if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        } else if (targetIds.isEmpty() && entry.getCard().getTargetGroups().size() == 1) {
            targetIds = entry.getTargetIds();
        }

        List<UUID> tokenControllers = new java.util.ArrayList<>();
        List<MovedCard> moved = new java.util.ArrayList<>();
        List<LastKnownCardStats> known = new java.util.ArrayList<>();
        for (UUID targetId : targetIds) {
            if (targetId == null) {
                continue;
            }
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            // Capture the controller before exile (needed for token creation)
            UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());

            if (controllerId != null) {
                entry.getRemovedPermanentControllers().put(targetId, controllerId);
            }
            // Later steps of the same resolution read the exiled permanent's last known information.
            entry.rememberLastKnownPermanentCard(targetId, target.getCard());
            entry.getLastKnownPermanentPowers().put(targetId, gameQueryService.getEffectivePower(gameData, target));
            entry.getLastKnownPermanentToughnesses().put(targetId,
                    gameQueryService.getEffectiveToughness(gameData, target));
            List<Card> cards = leavingCards(target);
            LastKnownCardStats lastKnown = asLastKnown(gameData, target);
            if (permanentRemovalService.removePermanentToExile(gameData, target)) {
                exiled.add(targetId);
                known.add(lastKnown);
                if (controllerId != null) {
                    exiledControllers.add(controllerId);
                }
                addMovedCards(gameData, targetId, cards, moved);
            }
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is exiled."));
            log.info("Game {} - {} is exiled by {}",
                    gameData.id, target.getCard().getName(), entry.getCard().getName());

            // Create a token for the exiled permanent's controller if specified
            if (exile.tokenForController() != null && controllerId != null) {
                tokenControllers.add(controllerId);
            }
        }

        entry.recordStep(StepKind.EXILE,
                StepOutcome.ofObjects(exiled, exiledControllers, moved).withLastKnownPermanents(known));
        permanentRemovalService.removeOrphanedAuras(gameData);
        for (UUID controllerId : tokenControllers) {
            destructionSupport.createTokenForPlayer(gameData, controllerId, exile.tokenForController(),
                    entry.getCard().getName(), entry.getCard().getSetCode());
        }
    }

    /**
     * Exiles what a non-target subject names. Several permanents ("exile this creature and target creature") leave as
     * one event, so each one's leave abilities see the others leave with it (CR 603.10a); the target part of such a
     * subject keeps its controller and last-known card and power for later steps, as the target path does.
     */
    private void exileSubject(GameData gameData, StackEntry entry, ExileTargetPermanentEffect exile) {
        List<UUID> exiled = new java.util.ArrayList<>();
        List<UUID> exiledControllers = new java.util.ArrayList<>();
        List<MovedCard> moved = new java.util.ArrayList<>();
        List<LastKnownCardStats> known = new java.util.ArrayList<>();
        List<Permanent> named = subjectResolutionService.permanents(gameData, entry, exile, exile.subject());
        java.util.Set<UUID> targeted = new java.util.HashSet<>();
        if (exile.subject() instanceof PermanentSubject.AllOf allOf) {
            for (PermanentSubject part : allOf.subjects()) {
                if (part.declaresTarget()) {
                    targeted.addAll(subjectResolutionService.permanentIds(gameData, entry, exile, part));
                }
            }
        }
        boolean together = named.size() > 1;
        if (together) {
            permanentRemovalService.beginPermanentLeaveBatch(gameData);
        }
        try {
            // Each as it was before any of them left (CR 608.2h).
            named.forEach(permanent -> known.add(asLastKnown(gameData, permanent)));
            exileNamed(gameData, entry, exile, named, targeted, exiled, exiledControllers, moved);
        } finally {
            if (together) {
                permanentRemovalService.endPermanentLeaveBatch(gameData);
            }
        }
        known.removeIf(lastKnown -> !exiled.contains(lastKnown.cardId()));
        entry.recordStep(StepKind.EXILE,
                StepOutcome.ofObjects(exiled, exiledControllers, moved).withLastKnownPermanents(known));
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private LastKnownCardStats asLastKnown(GameData gameData, Permanent permanent) {
        return new LastKnownCardStats(permanent.getId(), gameQueryService.getEffectivePower(gameData, permanent),
                gameQueryService.getEffectiveToughness(gameData, permanent),
                gameQueryService.getPermanentManaValue(permanent),
                gameQueryService.getEffectiveName(gameData, permanent));
    }

    private void exileNamed(GameData gameData, StackEntry entry, ExileTargetPermanentEffect exile,
                            List<Permanent> named, java.util.Set<UUID> targeted, List<UUID> exiled,
                            List<UUID> exiledControllers, List<MovedCard> moved) {
        for (Permanent permanent : named) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
            if (targeted.contains(permanent.getId())) {
                if (controllerId != null) {
                    entry.getRemovedPermanentControllers().put(permanent.getId(), controllerId);
                }
                entry.rememberLastKnownPermanentCard(permanent.getId(), permanent.getCard());
                entry.getLastKnownPermanentPowers().put(permanent.getId(),
                        gameQueryService.getEffectivePower(gameData, permanent));
                entry.getLastKnownPermanentToughnesses().put(permanent.getId(),
                        gameQueryService.getEffectiveToughness(gameData, permanent));
            }
            if (exile.outsideGame()) {
                if (!permanentRemovalService.removePermanentToOutsideGame(gameData, permanent)) {
                    continue;
                }
                gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is removed from the game."));
            } else {
                List<Card> cards = leavingCards(permanent);
                if (!permanentRemovalService.removePermanentToExile(gameData, permanent)) {
                    continue;
                }
                gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " is exiled."));
                exiled.add(permanent.getId());
                addMovedCards(gameData, permanent.getId(), cards, moved);
                if (controllerId != null) {
                    exiledControllers.add(controllerId);
                }
            }
            log.info("Game {} - {} is exiled by {}",
                    gameData.id, permanent.getCard().getName(), entry.getCard().getName());
        }
    }

    /** The permanent's own card, then the other cards that leave the battlefield with it. */
    private static List<Card> leavingCards(Permanent permanent) {
        List<Card> cards = new java.util.ArrayList<>();
        if (permanent.getOriginalCard() != null) {
            cards.add(permanent.getOriginalCard());
        }
        for (Card card : permanent.cardsLeavingBattlefield()) {
            if (cards.stream().noneMatch(listed -> listed.getId().equals(card.getId()))) {
                cards.add(card);
            }
        }
        return cards;
    }

    /**
     * The cards an exiled permanent became in exile, for "the exiled card" (CR 400.7j): each of a merged permanent's
     * (CR 731.3c); none for a token.
     */
    private static void addMovedCards(GameData gameData, UUID permanentId, List<Card> cards, List<MovedCard> moved) {
        for (Card card : cards) {
            if (card.isToken()) {
                continue;
            }
            ExiledCardEntry exiled = gameData.findExiledCard(card.getId());
            if (exiled != null) {
                moved.add(new MovedCard(permanentId, card.getId(), Zone.EXILE, exiled.ownerId()));
            }
        }
    }
}
