package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Creates token copies of the effect's source (embalm, eternalize, offspring, Splinter Twin, ...) as one ordered
 * token creation event through {@link TokenCopySupport}, so the creating player orders the count replacements and
 * additions that apply to it (CR 616.1).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenCopyOfSourceEffect) effect;

        // If the source left the battlefield (e.g. a planeswalker at 0 loyalty after paying its cost), copy the
        // card stored on the stack entry (CR 113.7a: the ability exists independently of its source,
        // which uses its last known information).
        Card sourceCard;
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent sourceForRelativeValues = sourcePermanent != null
                ? sourcePermanent : entry.getSourcePermanentSnapshot();
        if (sourcePermanent != null) {
            sourceCard = sourcePermanent.getCard();
        } else {
            sourceCard = entry.getSourcePermanentSnapshot() != null
                    ? entry.getSourcePermanentSnapshot().getCard() : entry.getCard();
            if (sourceCard == null) {
                log.info("Game {} - Source permanent no longer on battlefield and no card reference", gameData.id);
                return;
            }
        }

        AmountContext amountContext = AmountContext.forStackEntry(entry, sourceForRelativeValues);
        int amount = amountEvaluationService.evaluate(gameData, e.amount(), amountContext);
        if (amount <= 0) {
            return;
        }

        Map<CounterType, Integer> entersWithCounters = e.initialPlusOnePlusOneCounters() > 0
                ? Map.of(CounterType.PLUS_ONE_PLUS_ONE, e.initialPlusOnePlusOneCounters()) : Map.of();
        // "Except it has haste" is part of the copy (CR 707.9b), so it is an added keyword rather than a grant.
        CreateTokenCopyOfTargetPermanentEffect copyEffect = new CreateTokenCopyOfTargetPermanentEffect(
                e.addedSubtype() == null ? List.of() : List.of(e.addedSubtype()),
                e.additionalTypes() == null ? Set.of() : e.additionalTypes(),
                e.powerOverride(), e.toughnessOverride(), entersWithCounters,
                false, e.exileAtEndStep(), false, e.tappedAndAttacking(), false, false,
                e.colorOverride(), e.grantHaste() ? Set.of(Keyword.HASTE) : Set.of(),
                false, List.of(), List.of(), e.tapped(), e.removeLegendary(), new Fixed(1),
                e.exileAtEndOfCombat(), Set.of(), false, List.of());

        // "Put N counters on that token" is measured as the effect resolves and done once the tokens have entered;
        // the creating effect's instructions apply to every token of the event (Queen Allenal ruling).
        Map<CounterType, Integer> putCounters = new EnumMap<>(CounterType.class);
        if (e.initialCounters() != null) {
            e.initialCounters().forEach((counterType, counterAmount) -> {
                int count = amountEvaluationService.evaluate(gameData, counterAmount, amountContext);
                if (count > 0) {
                    putCounters.put(counterType, count);
                }
            });
        }

        tokenCopySupport.createSourceTokenCopiesThen(gameData, entry, sourceCard, amount, sourcePermanent,
                copyEffect, copyException(e), e.tappedAndAttacking(),
                putCounters.isEmpty() ? null : (game, tokens) -> putCounters(game, entry, tokens, putCounters));
    }

    /** The parts of the copy exception the shared token-copy effect doesn't express, or {@code null}. */
    private static Consumer<Card> copyException(CreateTokenCopyOfSourceEffect e) {
        boolean excludesEffects = e.excludedEffectType() != null
                || (e.additionalExcludedEffectTypes() != null && !e.additionalExcludedEffectTypes().isEmpty());
        boolean excludesKeywords = e.excludedKeywords() != null && !e.excludedKeywords().isEmpty();
        if (!e.removeManaCost() && e.startingLoyaltyOverride() == null && !excludesEffects && !excludesKeywords) {
            return null;
        }
        return tokenCard -> {
            if (e.removeManaCost()) {
                tokenCard.setManaCost("");
            }
            if (e.startingLoyaltyOverride() != null) {
                tokenCard.setLoyalty(e.startingLoyaltyOverride());
            }
            if (excludesKeywords && tokenCard.getKeywords() != null) {
                Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
                keywords.addAll(tokenCard.getKeywords());
                keywords.removeAll(e.excludedKeywords());
                tokenCard.setKeywords(keywords);
            }
            if (excludesEffects) {
                tokenCard.removeEffectsIf(copied -> copied.getClass() == e.excludedEffectType()
                        || (e.additionalExcludedEffectTypes() != null
                        && e.additionalExcludedEffectTypes().contains(copied.getClass())));
            }
        };
    }

    private void putCounters(GameData gameData, StackEntry entry, List<Permanent> tokens,
                             Map<CounterType, Integer> counters) {
        for (Permanent token : tokens) {
            Permanent onBattlefield = gameQueryService.findPermanentById(gameData, token.getId());
            if (onBattlefield == null || gameQueryService.cantHaveCounters(gameData, onBattlefield)) {
                continue;
            }
            counters.forEach((counterType, count) ->
                    permanentCounterSupport.placeCounterOnPermanent(gameData, entry, onBattlefield, counterType, count));
        }
    }
}
