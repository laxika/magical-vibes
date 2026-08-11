package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Shared permanent-control/token helpers used by every "normal" Permanent Control effect handler
 * and by other services (mill, counter, misc triggers).
 *
 * <p>Extracted verbatim from {@code PermanentControlResolutionService}; behavior is identical.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermanentControlSupport {

    private final BattlefieldEntryService battlefieldEntryService;
    private final LegendRuleService legendRuleService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    public List<UUID> applyCreateToken(GameData gameData, UUID controllerId, CreateTokenEffect token, String sourceSetCode) {
        if (!(token.amount() instanceof Fixed fixed)) {
            throw new IllegalStateException("Dynamic token counts must be evaluated before applyCreateToken: " + token.amount());
        }
        return applyCreateToken(gameData, controllerId, token, fixed.value(), sourceSetCode);
    }

    /**
     * Creates {@code amount} tokens from the blueprint; the count is already evaluated by the caller.
     * Returns the ids of the created token permanents (used by callers that must act on the new
     * tokens later in the same resolution, e.g. Gilt-Leaf Ambush's clash-win deathtouch grant).
     */
    public List<UUID> applyCreateToken(GameData gameData, UUID controllerId, CreateTokenEffect token, int amount, String sourceSetCode) {
        return applyCreateToken(gameData, controllerId, token, amount, sourceSetCode,
                fixedStat(token.power(), token), fixedStat(token.toughness(), token));
    }

    private static int fixedStat(DynamicAmount stat, CreateTokenEffect token) {
        if (!(stat instanceof Fixed fixed)) {
            throw new IllegalStateException(
                    "Dynamic token power/toughness must be evaluated before applyCreateToken: " + token.tokenName());
        }
        return fixed.value();
    }

    /**
     * Creates {@code amount} tokens with the given already-evaluated power/toughness (dynamic-P/T
     * blueprints like Phyrexian Rebirth's X/X token are resolved by {@code CreateTokenEffectHandler}).
     */
    public List<UUID> applyCreateToken(GameData gameData, UUID controllerId, CreateTokenEffect token, int amount,
                                       String sourceSetCode, int power, int toughness) {
        List<UUID> createdIds = new ArrayList<>();
        Set<Keyword> grantedKeywordsUntilEndOfTurn = token.grantedKeywordsUntilEndOfTurn();
        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, controllerId);
        int totalAmount = amount * tokenMultiplier;
        Set<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
        enterTappedTypesSnapshot.addAll(battlefieldEntryService.snapshotEnterTappedTypes(gameData));
        // CR 614.12: all tokens from one effect are created simultaneously, so none of them may
        // apply its own replacement/static abilities to the others as they enter.
        List<Permanent> batch = new ArrayList<>();
        boolean isCreature = token.primaryType() == CardType.CREATURE;
        for (int i = 0; i < totalAmount; i++) {
            Card tokenCard = TokenCardFactory.create(token, power, toughness, sourceSetCode);

            Permanent tokenPermanent = new Permanent(tokenCard);
            if (token.initialPlusOnePlusOneCounters() > 0
                    && !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, tokenPermanent, controllerId)) {
                int initial = token.initialPlusOnePlusOneCounters();
                if (isCreature) {
                    initial = gameQueryService.doublePlusOnePlusOneCounters(
                            gameData, tokenPermanent, controllerId, initial);
                }
                if (initial > 0) {
                    tokenPermanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, initial);
                }
            }
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, controllerId, tokenPermanent, enterTappedTypesSnapshot, batch);
            batch.add(tokenPermanent);
            createdIds.add(tokenPermanent.getId());

            if (token.tappedAndAttacking()) {
                tokenPermanent.tap();
                tokenPermanent.setAttacking(true);
            } else if (token.tapped()) {
                tokenPermanent.tap();
            }

            if (grantedKeywordsUntilEndOfTurn != null && !grantedKeywordsUntilEndOfTurn.isEmpty()) {
                tokenPermanent.getGrantedKeywords().addAll(grantedKeywordsUntilEndOfTurn);
            }

            if (token.exileAtEndOfCombat()) {
                gameData.queueDelayedAction(new DelayedPermanentAction(tokenPermanent.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT));
            }
            if (token.exileAtEndStep()) {
                gameData.queueDelayedAction(new DelayedPermanentAction(tokenPermanent.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP));
            }

            String colorDesc;
            if (token.colors() != null && !token.colors().isEmpty()) {
                colorDesc = token.colors().stream()
                        .map(c -> c.name().charAt(0) + c.name().substring(1).toLowerCase())
                        .reduce((a, b) -> a + " and " + b).orElse("");
                colorDesc += " ";
            } else {
                colorDesc = "";
            }

            if (isCreature) {
                String tappedAttackingDesc = token.tappedAndAttacking() ? " tapped and attacking" : (token.tapped() ? " tapped" : "");
                String logEntry = "A " + power + "/" + toughness + " " + colorDesc + token.tokenName() + " creature token enters the battlefield" + tappedAttackingDesc + ".";
                gameLogService.append(gameData, GameLog.text(logEntry));

                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null, false);
                if (!gameData.interaction.isAwaitingInput()) {
                    legendRuleService.checkLegendRule(gameData, controllerId);
                }
            } else {
                String tokenTypeDesc = token.primaryType().name().charAt(0) + token.primaryType().name().substring(1).toLowerCase();
                String logEntry = "A " + colorDesc + token.tokenName() + " " + tokenTypeDesc.toLowerCase() + " token enters the battlefield.";
                gameLogService.append(gameData, GameLog.text(logEntry));

                // Fire ally-artifact / equipment / etc. enters triggers (e.g. Voldaren Bloodcaster
                // watching Blood tokens). Same entry pipeline as creature tokens — type checks
                // inside BattlefieldEntryService gate creature-only slots.
                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null, false);
                if (!gameData.interaction.isAwaitingInput()) {
                    legendRuleService.checkLegendRule(gameData, controllerId);
                }
            }
        }

        battlefieldEntryService.checkAllyTokenEntersTriggers(gameData, controllerId, createdIds.size());

        log.info("Game {} - {} {} token(s) created for player {}", gameData.id, totalAmount, token.tokenName(), controllerId);
        return createdIds;
    }
}
