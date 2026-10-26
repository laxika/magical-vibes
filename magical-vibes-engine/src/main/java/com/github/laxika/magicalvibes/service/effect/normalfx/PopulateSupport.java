package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.replacement.TokenCreationEvent;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.replacement.ReplacementOrderService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * The two halves of populate (CR 701.36a): finding the creature tokens a player controls, and
 * creating the copy of the one they chose.
 *
 * <p>{@link PopulateEffectHandler} needs the first half to decide between "no token, do nothing",
 * "one token, forced choice" and "prompt"; {@code PermanentChoiceBattlefieldHandlerService} needs
 * the second half once the prompt is answered.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PopulateSupport {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final ObjectProvider<ReplacementOrderService> replacementOrderService;
    private final ObjectProvider<PermanentControlSupport> permanentControlSupport;

    /** The creature tokens {@code controllerId} controls that can be chosen — the legal populate choices. */
    public List<Permanent> creatureTokensControlledBy(GameData gameData, UUID controllerId) {
        List<Permanent> tokens = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return tokens;
        }
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isToken(gameData, permanent) && gameQueryService.isCreature(gameData, permanent)
                    && !gameQueryService.cantBeChosen(gameData, permanent)) {
                tokens.add(permanent);
            }
        }
        return tokens;
    }

    /**
     * Creates a token copy of {@code sourceToken} for {@code controllerId} as a token creation event whose count
     * replacements and additions the player orders (CR 616.1). The creation may pause for that choice and then
     * continue through {@link #createCopiesAfterReplacementOrder}.
     */
    public void createCopy(GameData gameData, UUID controllerId, Permanent sourceToken, StackEntry entry) {
        Card sourceCard = sourceToken.getCard();
        TokenCreationEvent event = TokenCreationEvent.copies(controllerId, 1, sourceCard,
                TokenCreationReplacementSupport.copyKind(sourceCard),
                TokenCreationReplacementSupport.entersAsCreature(sourceCard),
                entry == null || entry.getCard() == null ? null : entry.getCard().getSetCode());
        var outcome = replacementOrderService.getObject().run(gameData, event);
        if (outcome instanceof ReplacementOrderService.Outcome.Completed<TokenCreationEvent> completed) {
            createCopiesAfterReplacementOrder(gameData, completed.event(), entry);
        }
    }

    /**
     * Creates the copies and the added tokens of a populate event whose replacements have all been applied in the
     * order the creating player chose, recording them on {@code entry} when there is one.
     */
    public void createCopiesAfterReplacementOrder(GameData gameData, TokenCreationEvent event, StackEntry entry) {
        UUID controllerId = event.controllerId();
        Card sourceCard = event.copyOf();
        for (int copy = 0; copy < event.tokenCount(); copy++) {
            Card tokenCard = CreateTokenCopyOfTargetPermanentEffectHandler.buildTokenCopyCard(
                    sourceCard, new CreateTokenCopyOfTargetPermanentEffect());
            tokenCard = TokenCreationReplacementSupport.replaceCreatureTokenIfApplicable(
                    gameData, controllerId, tokenCard);
            Permanent tokenPermanent = new Permanent(tokenCard);
            if (MakeCreatedPermanentsAttackingEffectHandler.entersTappedAndAttacking(entry)) {
                tokenPermanent.tap();
            }
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, tokenPermanent);
            if (entry != null) {
                entry.getCreatedPermanentIds().add(tokenPermanent.getId());
            }

            gameLogService.append(gameData, GameLog.textCardText("A token copy of ", sourceCard, " is created."));
            log.info("Game {} - Populate creates a token copy of {}", gameData.id, sourceCard.getName());

            // Pass null targetId: the token wasn't cast, so no target was chosen. Any targeted
            // ETB ability chooses its target at trigger time (CR 603.3) via the ETBTokenTargetTrigger path.
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null, false);
            battlefieldEntryService.checkAllyTokenEntersTriggers(
                    gameData, controllerId, List.of(tokenPermanent.getId()));
        }

        if (!event.addedTokens().isEmpty()) {
            List<UUID> addedIds = permanentControlSupport.getObject()
                    .applyCreateTokenAfterReplacementOrder(gameData, event.withTokenCount(0));
            if (entry != null) {
                entry.getCreatedPermanentIds().addAll(addedIds);
            }
        }
    }
}
