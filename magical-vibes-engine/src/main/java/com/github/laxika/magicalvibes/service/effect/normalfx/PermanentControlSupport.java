package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AddClueTokenToTokenCreationEffect;
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
        return applyCreateToken(gameData, controllerId, token, amount, sourceSetCode, power, toughness,
                true, true, true);
    }

    private List<UUID> applyCreateToken(GameData gameData, UUID controllerId, CreateTokenEffect token, int amount,
                                        String sourceSetCode, int power, int toughness,
                                        boolean applyAdditionalReplacements, boolean applyTokenMultiplier,
                                        boolean fireTokenTriggers) {
        List<UUID> createdIds = new ArrayList<>();
        boolean baseTokenIsCreature = token.primaryType() == CardType.CREATURE;
        int tokenMultiplier = applyTokenMultiplier
                ? gameQueryService.getTokenMultiplier(gameData, controllerId, baseTokenIsCreature) : 1;
        int totalAmount = amount * tokenMultiplier;
        CreateTokenEffect additionalFrog = applyAdditionalReplacements
                ? TokenCreationReplacementSupport.additionalFrogTokenIfApplicable(
                        gameData, controllerId, token)
                : null;
        int totalTokens = totalAmount + (additionalFrog != null && totalAmount > 0 ? 1 : 0);
        boolean addClueToken = applyAdditionalReplacements
                && totalAmount > 0
                && hasSolvedClueReplacement(gameData, controllerId);
        Set<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
        enterTappedTypesSnapshot.addAll(battlefieldEntryService.snapshotEnterTappedTypes(gameData));
        // CR 614.12: all tokens from one effect are created simultaneously, so none of them may
        // apply its own replacement/static abilities to the others as they enter.
        List<Permanent> batch = new ArrayList<>();
        for (int i = 0; i < totalTokens; i++) {
            boolean isAdditionalFrog = i >= totalAmount;
            CreateTokenEffect tokenToCreate = isAdditionalFrog ? additionalFrog : token;
            int tokenPower = isAdditionalFrog ? 1 : power;
            int tokenToughness = isAdditionalFrog ? 1 : toughness;
            boolean isCreature = tokenToCreate.primaryType() == CardType.CREATURE;
            Card tokenCard = TokenCardFactory.create(tokenToCreate, tokenPower, tokenToughness, sourceSetCode);
            tokenCard = TokenCreationReplacementSupport.replaceCreatureTokenIfApplicable(
                    gameData, controllerId, tokenCard);

            Permanent tokenPermanent = new Permanent(tokenCard);
            if (tokenToCreate.initialPlusOnePlusOneCounters() > 0
                    && !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, tokenPermanent, controllerId)) {
                int initial = tokenToCreate.initialPlusOnePlusOneCounters();
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

            if (tokenToCreate.tappedAndAttacking()) {
                tokenPermanent.tap();
                tokenPermanent.setAttacking(true);
            } else if (tokenToCreate.tapped()) {
                tokenPermanent.tap();
            }

            Set<Keyword> grantedKeywordsUntilEndOfTurn = tokenToCreate.grantedKeywordsUntilEndOfTurn();
            if (grantedKeywordsUntilEndOfTurn != null && !grantedKeywordsUntilEndOfTurn.isEmpty()) {
                tokenPermanent.getGrantedKeywords().addAll(grantedKeywordsUntilEndOfTurn);
            }

            if (tokenToCreate.exileAtEndOfCombat()) {
                gameData.queueDelayedAction(new DelayedPermanentAction(tokenPermanent.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT));
            }
            if (tokenToCreate.exileAtEndStep()) {
                gameData.queueDelayedAction(new DelayedPermanentAction(tokenPermanent.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP));
            }

            String colorDesc;
            if (tokenToCreate.colors() != null && !tokenToCreate.colors().isEmpty()) {
                colorDesc = tokenToCreate.colors().stream()
                        .map(c -> c.name().charAt(0) + c.name().substring(1).toLowerCase())
                        .reduce((a, b) -> a + " and " + b).orElse("");
                colorDesc += " ";
            } else {
                colorDesc = "";
            }

            if (isCreature) {
                String tappedAttackingDesc = tokenToCreate.tappedAndAttacking() ? " tapped and attacking" : (tokenToCreate.tapped() ? " tapped" : "");
                String logEntry = "A " + tokenPower + "/" + tokenToughness + " " + colorDesc + tokenToCreate.tokenName() + " creature token enters the battlefield" + tappedAttackingDesc + ".";
                gameLogService.append(gameData, GameLog.text(logEntry));

                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null, false);
                if (!gameData.interaction.isAwaitingInput()) {
                    legendRuleService.checkLegendRule(gameData, controllerId);
                }
            } else {
                String tokenTypeDesc = tokenToCreate.primaryType().name().charAt(0) + tokenToCreate.primaryType().name().substring(1).toLowerCase();
                String logEntry = "A " + colorDesc + tokenToCreate.tokenName() + " " + tokenTypeDesc.toLowerCase() + " token enters the battlefield.";
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

        if (addClueToken) {
            createdIds.addAll(applyCreateToken(gameData, controllerId, CreateTokenEffect.ofClueToken(1), 1,
                    sourceSetCode, 0, 0, false, false, false));
        }

        UUID tokenControllerId = createdIds.isEmpty()
                ? controllerId
                : gameQueryService.findPermanentController(gameData, createdIds.get(createdIds.size() - 1));
        if (fireTokenTriggers) {
            battlefieldEntryService.checkAllyTokenEntersTriggers(
                    gameData, tokenControllerId != null ? tokenControllerId : controllerId, createdIds);
        }

        log.info("Game {} - {} token(s) created for player {}", gameData.id, createdIds.size(), controllerId);
        return createdIds;
    }

    private boolean hasSolvedClueReplacement(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return false;
        }
        return battlefield.stream()
                .filter(permanent -> permanent.isSolved()
                        && !permanent.isLosesAllAbilitiesUntilEndOfTurn()
                        && !permanent.isStaticEffectSuppressed(AddClueTokenToTokenCreationEffect.class))
                .flatMap(permanent -> permanent.getCard().getEffects(EffectSlot.STATIC).stream())
                .anyMatch(AddClueTokenToTokenCreationEffect.class::isInstance);
    }
}
