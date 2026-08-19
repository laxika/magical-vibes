package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Shared token-copy creation used by target-copy effects and last-known-information riders. */
@Component
@RequiredArgsConstructor
public class TokenCopySupport {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;

    public void createTokenCopies(GameData gameData, StackEntry entry, List<Card> sourceCards,
                                  Permanent sourcePermanent,
                                  CreateTokenCopyOfTargetPermanentEffect effect) {
        createTokenCopies(gameData, entry, sourceCards, sourcePermanent, entry.getControllerId(), effect);
    }

    public void createTokenCopies(GameData gameData, StackEntry entry, List<Card> sourceCards,
                                  Permanent sourcePermanent, UUID tokenControllerId,
                                  CreateTokenCopyOfTargetPermanentEffect effect) {
        if (sourceCards == null || sourceCards.isEmpty()) {
            return;
        }

        int tokenMultiplier = gameQueryService.getTokenMultiplier(gameData, tokenControllerId);
        List<Permanent> tokens = new ArrayList<>();
        for (Card sourceCard : sourceCards) {
            for (int copy = 0; copy < tokenMultiplier; copy++) {
                Card tokenCard = buildTokenCopyCard(sourceCard, effect);
                tokenCard = TokenCreationReplacementSupport.replaceCreatureTokenIfApplicable(
                        gameData, tokenControllerId, tokenCard);
                tokens.add(new Permanent(tokenCard));
            }
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        for (Permanent tokenPermanent : tokens) {
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, tokenControllerId, tokenPermanent, enterTappedTypes, simultaneouslyEntered);
            entry.getCreatedPermanentIds().add(tokenPermanent.getId());
            if (effect.trackWithSource() && entry.getSourcePermanentId() != null) {
                gameData.sourceCreatedTokens
                        .computeIfAbsent(entry.getSourcePermanentId(), ignored -> ConcurrentHashMap.newKeySet())
                        .add(tokenPermanent.getId());
            }

            if (effect.tappedAndAttacking()) {
                tokenPermanent.tap();
                tokenPermanent.setAttacking(true);
                if (sourcePermanent != null) {
                    tokenPermanent.setAttackTarget(sourcePermanent.getAttackTarget());
                }
            }

            if (effect.exileAtEndStep()) {
                gameData.queueDelayedAction(new DelayedPermanentAction(
                        tokenPermanent.getId(), DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP));
            }
            if (effect.sacrificeAtEndStep()) {
                gameData.queueDelayedAction(new DelayedPermanentAction(
                        tokenPermanent.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
            }

            Card sourceCard = tokenPermanent.getCard();
            gameLogService.append(gameData, GameLog.textCardText("A token copy of ", sourceCard, " is created."));
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, tokenControllerId, sourceCard, null, false);

            if (effect.initialCounters() != null && !effect.initialCounters().isEmpty()
                    && !gameQueryService.cantHaveCounters(gameData, tokenPermanent)) {
                for (var counterEntry : effect.initialCounters().entrySet()) {
                    if (counterEntry.getValue() > 0) {
                        permanentCounterSupport.placeCounterOnPermanent(
                                gameData, entry, tokenPermanent, counterEntry.getKey(), counterEntry.getValue());
                    }
                }
            }
            simultaneouslyEntered.add(tokenPermanent);
        }
    }

    static Card buildTokenCopyCard(Card sourceCard, CreateTokenCopyOfTargetPermanentEffect effect) {
        boolean hasPTOverride = effect.powerOverride() != null || effect.toughnessOverride() != null;

        Card tokenCard = new Card();
        tokenCard.setName(sourceCard.getName());
        tokenCard.setType(sourceCard.getType());
        tokenCard.setAdditionalTypes(sourceCard.getAdditionalTypes());
        tokenCard.setManaCost(sourceCard.getManaCost() != null ? sourceCard.getManaCost() : "");
        tokenCard.setToken(true);
        CardColor color = effect.colorOverride() != null ? effect.colorOverride() : sourceCard.getColor();
        tokenCard.setColor(color);
        tokenCard.setColors(effect.colorOverride() != null
                ? List.of(effect.colorOverride())
                : sourceCard.getColors());
        tokenCard.setSupertypes(sourceCard.getSupertypes());
        tokenCard.setPower(effect.powerOverride() != null ? effect.powerOverride() : sourceCard.getPower());
        tokenCard.setToughness(effect.toughnessOverride() != null ? effect.toughnessOverride() : sourceCard.getToughness());
        tokenCard.setCardText(sourceCard.getCardText());
        tokenCard.setSetCode(sourceCard.getSetCode());
        tokenCard.setCollectorNumber(sourceCard.getCollectorNumber());

        List<CardSubtype> subtypes = new ArrayList<>();
        if (sourceCard.getSubtypes() != null) {
            subtypes.addAll(sourceCard.getSubtypes());
        }
        if (effect.additionalSubtypes() != null) {
            for (CardSubtype subtype : effect.additionalSubtypes()) {
                if (!subtypes.contains(subtype)) {
                    subtypes.add(subtype);
                }
            }
        }
        tokenCard.setSubtypes(subtypes);

        if (effect.additionalTypes() != null && !effect.additionalTypes().isEmpty()) {
            Set<CardType> merged = EnumSet.noneOf(CardType.class);
            merged.addAll(tokenCard.getAdditionalTypes());
            for (CardType additionalType : effect.additionalTypes()) {
                if (additionalType != tokenCard.getType() && !merged.contains(additionalType)) {
                    merged.add(additionalType);
                }
            }
            tokenCard.setAdditionalTypes(merged);
        }

        Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
        if (sourceCard.getKeywords() != null) {
            keywords.addAll(sourceCard.getKeywords());
        }
        if (effect.grantHaste()) {
            keywords.add(Keyword.HASTE);
        }
        if (effect.additionalKeywords() != null) {
            keywords.addAll(effect.additionalKeywords());
        }
        if (!keywords.isEmpty()) {
            tokenCard.setKeywords(keywords);
        }

        for (EffectSlot slot : EffectSlot.values()) {
            for (EffectRegistration registration : sourceCard.getEffectRegistrations(slot)) {
                if (hasPTOverride && registration.effect().isPowerToughnessDefining()) {
                    continue;
                }
                tokenCard.addEffect(slot, registration.effect(), registration.triggerMode());
            }
        }
        for (ActivatedAbility ability : sourceCard.getActivatedAbilities()) {
            tokenCard.addActivatedAbility(ability);
        }
        tokenCard.copyTargetingFrom(sourceCard);
        return tokenCard;
    }
}
