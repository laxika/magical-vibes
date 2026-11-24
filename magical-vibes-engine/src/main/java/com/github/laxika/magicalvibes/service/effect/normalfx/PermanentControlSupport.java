package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.AbilityEffects;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.replacement.TokenCreationEvent;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.replacement.ReplacementOrderService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.DelayedPermanentActions;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.github.laxika.magicalvibes.service.replacement.ReplacementOrderService replacementOrderService;

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
        return applyCreateToken(gameData, controllerId, token, amount, sourceSetCode, power, toughness, List.of());
    }

    /** As above, with {@code additionalAbilities} on each token as well. */
    public List<UUID> applyCreateToken(GameData gameData, UUID controllerId, CreateTokenEffect token, int amount,
                                       String sourceSetCode, int power, int toughness,
                                       List<Ability> additionalAbilities) {
        return createOrdered(gameData, controllerId, token, amount, sourceSetCode, power, toughness,
                additionalAbilities);
    }

    /**
     * Creates the tokens after the player creating them has ordered the count replacements, additions and Jinnie
     * Fay's replacement (CR 616.1), so the creation may pause for the order prompt or Jinnie Fay's choice and then
     * continue through {@link #applyCreateTokenAfterReplacementOrder}. An event creating no token is no event: nothing
     * replaces it.
     */
    private List<UUID> createOrdered(GameData gameData, UUID controllerId, CreateTokenEffect token, int amount,
                                     String sourceSetCode, int power, int toughness,
                                     List<Ability> additionalAbilities) {
        TokenCreationEvent event = new TokenCreationEvent(
                controllerId, Math.max(amount, 0), Set.of(), token, power, toughness, sourceSetCode,
                additionalAbilities, token.primaryType() == CardType.CREATURE, simultaneousTokenGroups(token));
        if (event.amount() <= 0) {
            return List.of();
        }
        var outcome = replacementOrderService.run(gameData, event);
        if (outcome instanceof ReplacementOrderService.Outcome.Completed<TokenCreationEvent> completed) {
            return applyCreateTokenAfterReplacementOrder(gameData, completed.event());
        }
        return List.of();
    }

    /**
     * Creates the tokens of an event whose count replacements and additions have all been applied, in
     * the order the creating player chose. The event's counts are final, so those replacements are not
     * applied again; the event's added tokens (simultaneous tokens and every addition's tokens) are
     * created with the blueprint's tokens.
     */
    public List<UUID> applyCreateTokenAfterReplacementOrder(GameData gameData, TokenCreationEvent event) {
        return createInOneEvent(gameData, event.controllerId(), event.token(), event.sourceSetCode(), event.power(),
                event.toughness(), event.additionalAbilities(), event.tokenCount(), event.addedTokens());
    }

    private static List<TokenCreationEvent.TokenGroup> simultaneousTokenGroups(CreateTokenEffect token) {
        List<TokenCreationEvent.TokenGroup> groups = new ArrayList<>();
        for (CreateTokenEffect simultaneousToken : token.simultaneousTokens()) {
            if (!(simultaneousToken.amount() instanceof Fixed fixedAmount)) {
                throw new IllegalStateException("Simultaneous token counts must be evaluated");
            }
            groups.add(new TokenCreationEvent.TokenGroup(simultaneousToken, fixedAmount.value()));
        }
        return groups;
    }

    /**
     * Creates {@code tokenCount} tokens of {@code token} and every token of {@code addedTokens}: the event's final
     * tokens, after its replacements were ordered and applied, so none of them applies again.
     */
    private List<UUID> createInOneEvent(GameData gameData, UUID controllerId, CreateTokenEffect token,
                                        String sourceSetCode, int power, int toughness,
                                        List<Ability> additionalAbilities, int tokenCount,
                                        List<TokenCreationEvent.TokenGroup> addedTokens) {
        // The tokens one creation puts onto the battlefield enter in one event (CR 603.2c): a "one or more" ability
        // that sees them triggers once.
        boolean alreadyCollectingEntries = gameData.collectingSimultaneousCreatureEntryTriggers;
        gameData.collectingSimultaneousCreatureEntryTriggers = true;
        if (!alreadyCollectingEntries) {
            gameData.simultaneousCreatureEntryTriggers.clear();
        }
        gameData.rulesEvents.beginBatch();
        try {
            return createTokens(gameData, controllerId, token, sourceSetCode, power, toughness, additionalAbilities,
                    tokenCount, addedTokens);
        } finally {
            gameData.rulesEvents.endBatch();
            gameData.collectingSimultaneousCreatureEntryTriggers = alreadyCollectingEntries;
            if (!alreadyCollectingEntries) {
                gameData.simultaneousCreatureEntryTriggers.clear();
            }
        }
    }

    private List<UUID> createTokens(GameData gameData, UUID controllerId, CreateTokenEffect token, String sourceSetCode,
                                    int power, int toughness, List<Ability> additionalAbilities, int tokenCount,
                                    List<TokenCreationEvent.TokenGroup> addedTokens) {
        List<UUID> createdIds = new ArrayList<>();
        CreateTokenEffect evaluatedToken = token.withPowerToughness(power, toughness);
        Set<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
        enterTappedTypesSnapshot.addAll(battlefieldEntryService.snapshotEnterTappedTypes(gameData));
        // CR 614.12: all tokens from one effect are created simultaneously, so none of them may
        // apply its own replacement/static abilities to the others as they enter.
        List<Permanent> batch = new ArrayList<>();
        List<CreateTokenEffect> tokenBlueprints = new ArrayList<>();
        for (int i = 0; i < tokenCount; i++) {
            tokenBlueprints.add(evaluatedToken);
        }
        for (TokenCreationEvent.TokenGroup group : addedTokens) {
            for (int tokenIndex = 0; tokenIndex < group.count(); tokenIndex++) {
                tokenBlueprints.add(group.blueprint());
            }
        }

        boolean hasNativeDevour = tokenBlueprints.stream().anyMatch(blueprint ->
                blueprint.tokenEffects() != null && blueprint.tokenEffects().stream()
                        .filter(com.github.laxika.magicalvibes.model.StaticAbility.class::isInstance)
                        .flatMap(ability -> AbilityEffects.effectsOf(ability).stream())
                        .anyMatch(com.github.laxika.magicalvibes.model.effect.DevourEffect.class::isInstance));
        List<com.github.laxika.magicalvibes.model.BattlefieldEntryCard> nativeEntries = new ArrayList<>();
        for (CreateTokenEffect originalTokenBlueprint : tokenBlueprints) {
            boolean originalBlueprint = originalTokenBlueprint == evaluatedToken;
            // The ordered event already applied Divine Visitation and Fisher's Talent in the creator's order (CR 616.1).
            CreateTokenEffect tokenBlueprint = originalTokenBlueprint;
            boolean blueprintIsCreature = tokenBlueprint.primaryType() == CardType.CREATURE;
            int blueprintPower = fixedStat(tokenBlueprint.power(), tokenBlueprint);
            int blueprintToughness = fixedStat(tokenBlueprint.toughness(), tokenBlueprint);
            Card tokenCard = TokenCardFactory.create(tokenBlueprint, blueprintPower, blueprintToughness, sourceSetCode);
            if (originalBlueprint) {
                additionalAbilities.forEach(tokenCard::addAbility);
            }

            Permanent tokenPermanent = new Permanent(tokenCard);
            tokenPermanent.setTokenKindReplacementsApplied(true);
            if (tokenBlueprint.initialPlusOnePlusOneCounters() > 0
                    && !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, tokenPermanent, controllerId)) {
                int initial = gameQueryService.doublePlusOnePlusOneCounters(
                        gameData, tokenPermanent, controllerId, tokenBlueprint.initialPlusOnePlusOneCounters());
                if (initial > 0) {
                    tokenPermanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, initial);
                }
            }
            if (!hasNativeDevour) {
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, controllerId, tokenPermanent, enterTappedTypesSnapshot, batch);
            }
            batch.add(tokenPermanent);
            createdIds.add(tokenPermanent.getId());
            UUID enteringControllerId = gameQueryService.findPermanentController(gameData, tokenPermanent.getId());

            if (tokenBlueprint.tappedAndAttacking()) {
                if (!tokenBlueprint.attackingUntapped()) {
                    tokenPermanent.tap();
                }
                tokenPermanent.enterAttacking(gameData.activePlayerId.equals(enteringControllerId)
                        && gameData.currentStep != null && gameData.currentStep.isCombatPhase());
            } else if (tokenBlueprint.tapped()) {
                tokenPermanent.tap();
            }

            Set<Keyword> blueprintGrantedKeywords = tokenBlueprint.grantedKeywordsUntilEndOfTurn();
            if (blueprintGrantedKeywords != null && !blueprintGrantedKeywords.isEmpty()) {
                gameData.addEffectOnPermanent(FaceDownVisibility.publicName(gameData, tokenPermanent), null, controllerId,
                        new GrantKeywordEffect(Set.copyOf(blueprintGrantedKeywords), Subjects.target()),
                        tokenPermanent.getId(), Duration.UNTIL_END_OF_TURN);
            }

            if (tokenBlueprint.exileAtEndOfCombat()) {
                DelayedPermanentActions.scheduleForResolution(gameData, controllerId, tokenPermanent,
                        new DelayedPermanentAction(tokenPermanent.getId(),
                                DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT),
                        ActivePlayer.ANY);
            }
            if (tokenBlueprint.exileAtEndStep()) {
                DelayedPermanentActions.scheduleForResolution(gameData, controllerId, tokenPermanent,
                        new DelayedPermanentAction(tokenPermanent.getId(),
                                DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP),
                        ActivePlayer.ANY);
            }

            String colorDesc;
            Set<CardColor> logColors = tokenBlueprint.colors() != null && !tokenBlueprint.colors().isEmpty()
                    ? tokenBlueprint.colors()
                    : tokenBlueprint.color() != null ? Set.of(tokenBlueprint.color()) : Set.of();
            if (!logColors.isEmpty()) {
                colorDesc = logColors.stream()
                        .map(c -> c.name().charAt(0) + c.name().substring(1).toLowerCase())
                        .reduce((a, b) -> a + " and " + b).orElse("");
                colorDesc += " ";
            } else {
                colorDesc = "";
            }

            if (blueprintIsCreature) {
                String tappedAttackingDesc = tokenBlueprint.tappedAndAttacking() ? " tapped and attacking" : (tokenBlueprint.tapped() ? " tapped" : "");
                String logEntry = "A " + blueprintPower + "/" + blueprintToughness + " " + colorDesc + tokenBlueprint.tokenName() + " creature token enters the battlefield" + tappedAttackingDesc + ".";
            if (!hasNativeDevour) gameLogService.append(gameData, GameLog.text(logEntry));

                if (!hasNativeDevour) battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, tokenPermanent.getCard(), null, false);
            } else {
                String tokenTypeDesc = tokenBlueprint.primaryType().name().charAt(0) + tokenBlueprint.primaryType().name().substring(1).toLowerCase();
                String logEntry = "A " + colorDesc + tokenBlueprint.tokenName() + " " + tokenTypeDesc.toLowerCase() + " token enters the battlefield.";
                if (!hasNativeDevour) gameLogService.append(gameData, GameLog.text(logEntry));

                // Fire ally-artifact / equipment / etc. enters triggers (e.g. Voldaren Bloodcaster
                // watching Blood tokens). Same entry pipeline as creature tokens — type checks
                // inside BattlefieldEntryService gate creature-only slots.
                if (!hasNativeDevour) battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, tokenPermanent.getCard(), null, false);
            }
            if (hasNativeDevour) {
                nativeEntries.add(new com.github.laxika.magicalvibes.model.BattlefieldEntryCard(
                        controllerId, controllerId, tokenCard, com.github.laxika.magicalvibes.model.Zone.STACK,
                        null, null, tokenPermanent, List.of(), tokenBlueprint.tapped()
                                || (tokenBlueprint.tappedAndAttacking() && !tokenBlueprint.attackingUntapped())));
            }
        }

        if (hasNativeDevour) battlefieldEntryBatchSupport.begin(gameData, nativeEntries);


        log.info("Game {} - {} token(s) created for player {}", gameData.id, createdIds.size(), controllerId);
        return createdIds;
    }
}
