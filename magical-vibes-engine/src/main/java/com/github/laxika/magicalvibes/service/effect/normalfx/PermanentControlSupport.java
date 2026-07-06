package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.scryfall.ScryfallOracleLoader;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Map;
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
    private final GameBroadcastService gameBroadcastService;

    public void applyCreateToken(GameData gameData, UUID controllerId, CreateTokenEffect token, String sourceSetCode) {
        if (!(token.amount() instanceof Fixed fixed)) {
            throw new IllegalStateException("Dynamic token counts must be evaluated before applyCreateToken: " + token.amount());
        }
        applyCreateToken(gameData, controllerId, token, fixed.value(), sourceSetCode);
    }

    /** Creates {@code amount} tokens from the blueprint; the count is already evaluated by the caller. */
    public void applyCreateToken(GameData gameData, UUID controllerId, CreateTokenEffect token, int amount, String sourceSetCode) {
        Set<Keyword> grantedKeywordsUntilEndOfTurn = token.grantedKeywordsUntilEndOfTurn();
        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, controllerId);
        int totalAmount = amount * tokenMultiplier;
        Set<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
        enterTappedTypesSnapshot.addAll(battlefieldEntryService.snapshotEnterTappedTypes(gameData));
        boolean isCreature = token.primaryType() == CardType.CREATURE;
        for (int i = 0; i < totalAmount; i++) {
            Card tokenCard = new Card();
            tokenCard.setName(token.tokenName());
            tokenCard.setType(token.primaryType());
            tokenCard.setManaCost("");
            tokenCard.setToken(true);
            tokenCard.setColor(token.color());
            if (token.colors() != null && !token.colors().isEmpty()) {
                tokenCard.setColors(token.colors().stream().toList());
            }
            if (isCreature) {
                tokenCard.setPower(token.power());
                tokenCard.setToughness(token.toughness());
            }
            tokenCard.setSubtypes(token.subtypes());
            if (token.keywords() != null && !token.keywords().isEmpty()) {
                tokenCard.setKeywords(token.keywords());
            }
            if (token.additionalTypes() != null && !token.additionalTypes().isEmpty()) {
                tokenCard.setAdditionalTypes(token.additionalTypes());
            }
            if (token.legendary()) {
                tokenCard.setSupertypes(Set.of(CardSupertype.LEGENDARY));
            }
            if (token.tokenEffects() != null) {
                for (Map.Entry<EffectSlot, CardEffect> tokenEffect : token.tokenEffects().entrySet()) {
                    tokenCard.addEffect(tokenEffect.getKey(), tokenEffect.getValue());
                }
            }
            if (token.tokenAbilities() != null) {
                for (ActivatedAbility ability : token.tokenAbilities()) {
                    tokenCard.addActivatedAbility(ability);
                }
            }

            // Look up token image from Scryfall token set
            ScryfallOracleLoader.TokenImageData imageData;
            if (isCreature) {
                imageData = ScryfallOracleLoader.getTokenImage(
                        sourceSetCode, token.tokenName(), token.power(), token.toughness(), token.color()
                );
            } else {
                imageData = ScryfallOracleLoader.getTokenImage(
                        sourceSetCode, token.tokenName(), token.color()
                );
            }
            if (imageData != null) {
                tokenCard.setSetCode(imageData.setCode());
                tokenCard.setCollectorNumber(imageData.collectorNumber());
            }

            Permanent tokenPermanent = new Permanent(tokenCard);
            if (token.initialPlusOnePlusOneCounters() > 0
                    && !gameQueryService.cantHaveCounters(gameData, tokenPermanent)) {
                tokenPermanent.setCounterCount(
                        CounterType.PLUS_ONE_PLUS_ONE, token.initialPlusOnePlusOneCounters());
            }
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, tokenPermanent, enterTappedTypesSnapshot);

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
                gameData.pendingTokenExilesAtEndOfCombat.add(tokenPermanent.getId());
            }
            if (token.exileAtEndStep()) {
                gameData.pendingTokenExilesAtEndStep.add(tokenPermanent.getId());
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
                String logEntry = "A " + token.power() + "/" + token.toughness() + " " + colorDesc + token.tokenName() + " creature token enters the battlefield" + tappedAttackingDesc + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);

                battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, tokenCard, null, false);
                if (!gameData.interaction.isAwaitingInput()) {
                    legendRuleService.checkLegendRule(gameData, controllerId);
                }
            } else {
                String tokenTypeDesc = token.primaryType().name().charAt(0) + token.primaryType().name().substring(1).toLowerCase();
                String logEntry = "A " + colorDesc + token.tokenName() + " " + tokenTypeDesc.toLowerCase() + " token enters the battlefield.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
            }
        }

        log.info("Game {} - {} {} token(s) created for player {}", gameData.id, totalAmount, token.tokenName(), controllerId);
    }
}
