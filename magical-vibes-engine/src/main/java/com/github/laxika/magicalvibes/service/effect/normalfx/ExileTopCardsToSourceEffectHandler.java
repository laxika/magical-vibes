package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardsControlLossWatch;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExilePlayPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.ExileAccessScope;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Exiles the top N cards of one or more libraries, tracked "exiled with" the source permanent.
 * The effect's player subject, resolved by {@link SubjectResolutionService}, picks the exiling players
 * in the order they exile.
 */
@Slf4j
@Component
public class ExileTopCardsToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;
    private final ExileService exileService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final SubjectResolutionService subjectResolutionService;

    @Autowired
    public ExileTopCardsToSourceEffectHandler(GameQueryService gameQueryService,
                                              GameLogService gameLogService,
                                              AmountEvaluationService amountEvaluationService,
                                              ExileService exileService,
                                              PredicateEvaluationService predicateEvaluationService,
                                              SubjectResolutionService subjectResolutionService) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.amountEvaluationService = amountEvaluationService;
        this.exileService = exileService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.subjectResolutionService = subjectResolutionService;
    }

    public ExileTopCardsToSourceEffectHandler(GameQueryService gameQueryService,
                                              GameLogService gameLogService,
                                              ExileService exileService,
                                              SubjectResolutionService subjectResolutionService) {
        this(gameQueryService, gameLogService, null, exileService, null, subjectResolutionService);
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTopCardsToSourceEffect) effect;
        UUID controllerId = entry.getControllerId();

        // Find the source permanent so exiled cards are tracked "with" it.
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent sourcePermanent = sourcePermanentId != null
                ? gameQueryService.findPermanentById(gameData, sourcePermanentId) : null;
        if (sourcePermanentId == null) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            if (bf != null) {
                for (Permanent p : bf) {
                    if (p.getCard().getId().equals(entry.getCard().getId())) {
                        sourcePermanent = p;
                        sourcePermanentId = p.getId();
                        break;
                    }
                }
            }
        }

        Card sourceCard = sourcePermanent != null
                ? sourcePermanent.getCard() : entry.getTargetingCard();
        List<PersistentPermission> persistentPermissions = sourceCard
                .getStaticEffects().stream()
                .map(ExileTopCardsToSourceEffectHandler::persistentPermission)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (e.toGraveyardOnControlLoss() && sourcePermanent != null) {
            UUID currentControllerId = gameData.findControllerOf(sourcePermanent);
            gameData.exiledCardsToGraveyardOnControlLossWatch.put(sourcePermanentId,
                    new ExiledCardsControlLossWatch(currentControllerId, sourceCard));
        }

        List<UUID> exiledCardIds = new ArrayList<>();
        List<UUID> exilingPlayerIds = new ArrayList<>();
        for (UUID playerId : subjectResolutionService.players(gameData, entry, e, e.player())) {
            int before = exiledCardIds.size();
            exileTopCards(gameData, e, playerId, sourceCard, sourcePermanentId,
                    effectiveCount(gameData, entry, sourcePermanent, e), persistentPermissions,
                    controllerId, exiledCardIds);
            if (exiledCardIds.size() > before) {
                exilingPlayerIds.add(playerId);
            }
        }
        entry.setResolutionExiledCardIds(exiledCardIds);
        entry.recordStep(StepKind.EXILE, exiledCardIds.isEmpty() ? StepOutcome.NONE
                : StepOutcome.ofObjects(exiledCardIds, exilingPlayerIds));
    }

    private int effectiveCount(GameData gameData, StackEntry entry, Permanent sourcePermanent,
                               ExileTopCardsToSourceEffect effect) {
        if (effect.dynamicCount() == null) {
            return effect.count();
        }
        return Math.max(0, amountEvaluationService.evaluate(gameData, effect.dynamicCount(),
                AmountContext.forStackEntry(entry, sourcePermanent)));
    }

    /**
     * A static play permission that survives its source leaving the battlefield, together with the
     * condition (for example "during any turn you attacked with a commander") that must hold
     * whenever the exiled card is played, or {@code null} when the permission is unconditional.
     */
    private record PersistentPermission(AllowCastFromCardsExiledWithSourceEffect effect, Condition condition) {
    }

    private static PersistentPermission persistentPermission(CardEffect staticEffect) {
        Condition condition = null;
        CardEffect inner = staticEffect;
        if (staticEffect instanceof ConditionalEffect conditional) {
            condition = conditional.condition();
            inner = conditional.wrapped();
        }
        if (inner instanceof AllowCastFromCardsExiledWithSourceEffect permission
                && permission.persistsAfterSourceLeaves()) {
            return new PersistentPermission(permission, condition);
        }
        return null;
    }

    private void exileTopCards(GameData gameData, ExileTopCardsToSourceEffect e, UUID playerId,
                               Card sourceCard, UUID sourcePermanentId, int count,
                               List<PersistentPermission> persistentPermissions,
                               UUID sourceControllerId, List<UUID> exiledCardIds) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        if (deck == null) {
            return;
        }

        int toExile = Math.min(count, deck.size());
        for (int i = 0; i < toExile; i++) {
            Card card = deck.removeFirst();
            exiledCardIds.add(card.getId());
            if (e.faceDown()) {
                exileService.exileCardFaceDown(gameData, playerId, card, sourcePermanentId, sourceControllerId);
            } else if (persistentPermissions.isEmpty()) {
                exileService.exileCard(gameData, playerId, card, sourcePermanentId);
            } else {
                exileService.exileCard(gameData, playerId, card, sourcePermanentId, sourceControllerId);
            }
            if (e.faceDown() && (e.exilerMayLookWhileExiled()
                    || e.mayLookAtFaceDownCards() && sourcePermanentId == null)) {
                // A spell has no permanent whose face-down exile pile the view could reveal, so the
                // controller's "you may look at" permission is recorded per card instead.
                gameData.grantExileLookPermission(card.getId(), sourceControllerId);
            }
            if (e.markWithIntelCounters()) {
                gameData.addExiledCardCounters(card.getId(), CounterType.INTEL, 1);
            }
            for (PersistentPermission persistent : persistentPermissions) {
                AllowCastFromCardsExiledWithSourceEffect permission = persistent.effect();
                if (permission.filter() == null || (predicateEvaluationService != null
                        && predicateEvaluationService.matchesCardPredicate(card, permission.filter(), null))) {
                    UUID permittedPlayer = permission.accessScope() == ExileAccessScope.EXILER
                            ? playerId : sourceControllerId;
                    boolean thisTurnOnly = permission.thisTurnOnly();
                    gameData.grantExilePlayPermission(card.getId(), new ExilePlayPermissionEffect(permittedPlayer)
                                    .withCondition(persistent.condition())
                                    .withAnyManaType(permission.anyManaType() && !thisTurnOnly),
                            null, permittedPlayer, thisTurnOnly ? Duration.UNTIL_END_OF_TURN : Duration.PERMANENT);
                    if (permission.anyManaType() && thisTurnOnly) {
                        gameData.grantExileAnyManaType(card.getId(), Duration.UNTIL_END_OF_TURN);
                    }
                    break;
                }
            }
        }

        if (toExile > 0) {
            String playerName = gameData.playerIdToName.get(playerId);
            // Card names are never listed: for a face-down exile that would leak information
            // CR 406.3 keeps hidden.
            String visibility = e.faceDown() ? " face down" : "";
            gameLogService.append(gameData, GameLog.builder()
                    .text(playerName + " exiles the top " + toExile + " card"
                            + (toExile != 1 ? "s" : "") + " of their library" + visibility + " (")
                    .card(sourceCard).text(").").build());
            log.info("Game {} - {} exiles {} cards from library to {}",
                    gameData.id, playerName, toExile, sourceCard.getName());
        }
    }
}
