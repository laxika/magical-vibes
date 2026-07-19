package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardScanner;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exhaustiveness guard for {@link TargetPolarityClassifier}: every permanent-targeting
 * effect that any registered card puts in its SPELL or ON_ENTER_BATTLEFIELD slot must
 * classify to a non-null {@link TargetPolarity}. An unclassified shape falls into
 * {@code AiTargetSelector.chooseTarget}'s own-battlefield-first general fallback, which
 * silently aims harmful spells at the AI's own permanents (the Quicksilver Geyser / Stun
 * bug family). This test turns that silent regression into a build failure.
 */
class TargetPolarityGuardTest {

    @Test
    void everyPermanentTargetingSpellOrEtbEffectClassifies() {
        GameTestHarness harness = new GameTestHarness();
        GameData gd = harness.getGameData();
        UUID aiPlayerId = harness.getPlayer2().getId();
        AmountEvaluationService amountEvaluationService = new AmountEvaluationService(
                new PredicateEvaluationService(harness.getGameQueryService()), harness.getGameQueryService());
        TargetPolarityClassifier classifier = new TargetPolarityClassifier(amountEvaluationService);

        Map<String, List<String>> unclassified = new TreeMap<>();
        Set<Class<?>> seenCardClasses = new HashSet<>();
        for (List<CardPrinting> printings : CardScanner.scan().values()) {
            for (CardPrinting printing : printings) {
                Card card = printing.factory().get();
                if (!seenCardClasses.add(card.getClass())) {
                    continue;
                }
                for (EffectSlot slot : new EffectSlot[]{EffectSlot.SPELL, EffectSlot.ON_ENTER_BATTLEFIELD}) {
                    for (CardEffect effect : card.getEffects(slot)) {
                        if (!effect.targetSpec().category().includesPermanents()) {
                            continue;
                        }
                        if (classifier.classify(gd, effect, aiPlayerId) == null) {
                            List<String> cards = unclassified.computeIfAbsent(
                                    effect.getClass().getSimpleName(), k -> new ArrayList<>());
                            if (cards.size() < 5) {
                                cards.add(card.getClass().getSimpleName());
                            }
                        }
                    }
                }
            }
        }

        assertThat(unclassified)
                .withFailMessage("""
                        These permanent-targeting effect shapes have no TargetPolarity, so the AI \
                        would route them through the own-battlefield-first fallback (harmful spells \
                        would target the AI's own permanents). Add a rule for each to \
                        TargetPolarityClassifier.classify — HARMFUL_REMOVAL / HARMFUL_DAMAGE / \
                        HARMFUL aim at the opponent, BENEFICIAL at the AI's own board, NEUTRAL \
                        keeps the fallback deliberately. Unclassified (effect -> example cards): %s"""
                        .formatted(unclassified))
                .isEmpty();
    }
}
