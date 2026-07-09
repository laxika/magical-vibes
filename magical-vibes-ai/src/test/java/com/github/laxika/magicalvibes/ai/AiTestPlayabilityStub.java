package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileXCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

/**
 * Installs realistic answers for the two engine queries behind the AI's engine-backed
 * castability check ({@code isSpellCastable}), so the mock-wired AI unit tests stay meaningful
 * without pulling in the full engine stack (harness-based tests use the real engine):
 * <ul>
 *   <li>{@link GameBroadcastService#isCardPlayable} — plain affordability against the passed pool
 *       plus the test's stubbed cast-cost modifier, and the creature-mana requirement.</li>
 *   <li>{@link CastingCostService#canPayAdditionalSpellCosts} — non-mana additional-cost
 *       satisfiability (sacrifice / graveyard-exile) computed from the board, mirroring the real
 *       engine query so tests that stage an unpayable sacrifice cost still see the AI decline.</li>
 * </ul>
 */
final class AiTestPlayabilityStub {

    private AiTestPlayabilityStub() {
    }

    static void install(GameBroadcastService gameBroadcastService, CastingCostService castingCostService) {
        Mockito.lenient().when(castingCostService.canPayAdditionalSpellCosts(
                        any(GameData.class), any(UUID.class), any(Card.class)))
                .thenAnswer(inv -> {
                    GameData gameData = inv.getArgument(0);
                    UUID playerId = inv.getArgument(1);
                    Card card = inv.getArgument(2);
                    return canPayAdditionalSpellCosts(gameData, playerId, card);
                });
        Mockito.lenient().when(gameBroadcastService.isCardPlayable(
                        any(GameData.class), any(UUID.class), any(Card.class), any(ManaPool.class), anyInt()))
                .thenAnswer(inv -> {
                    GameData gameData = inv.getArgument(0);
                    UUID playerId = inv.getArgument(1);
                    Card card = inv.getArgument(2);
                    ManaPool pool = inv.getArgument(3);
                    int additionalGenericCost = inv.getArgument(4);
                    ManaCost cost = new ManaCost(card.getManaCost());
                    int modifier = castingCostService.getCastCostModifier(gameData, playerId, card)
                            + additionalGenericCost;
                    if (!cost.canPay(pool, modifier)) {
                        return false;
                    }
                    return !card.isRequiresCreatureMana() || cost.canPayCreatureOnly(pool, modifier);
                });
    }

    /**
     * Board-driven mirror of {@code CastingCostService.canPayAdditionalSpellCosts} for the
     * mock-wired suites: reads the card's SPELL-slot cost effects against the player's
     * battlefield/graveyard using the permanents' own card types (which these tests set directly,
     * matching their {@code gameQueryService.isCreature/isArtifact} stubs). Permanent-predicate
     * sacrifice costs aren't exercised by the mock suites and are treated as satisfiable.
     */
    private static boolean canPayAdditionalSpellCosts(GameData gameData, UUID playerId, Card card) {
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(playerId, List.of());
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(playerId, List.of());
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            switch (effect) {
                case SacrificeCreatureCost ignored -> {
                    if (battlefield.stream().noneMatch(p -> p.getCard().hasType(CardType.CREATURE))) return false;
                }
                case SacrificeArtifactCost ignored -> {
                    if (battlefield.stream().noneMatch(p -> p.getCard().hasType(CardType.ARTIFACT))) return false;
                }
                case ExileNCardsFromGraveyardCost cost -> {
                    long matchingCount = graveyard.stream()
                            .filter(c -> cost.requiredType() == null || c.hasType(cost.requiredType()))
                            .count();
                    if (matchingCount < cost.count()) return false;
                }
                case ExileCardFromGraveyardCost cost -> {
                    if (graveyard.stream().noneMatch(c ->
                            (cost.requiredType() == null || c.hasType(cost.requiredType()))
                                    && (cost.requiredSubtype() == null || c.getSubtypes().contains(cost.requiredSubtype())))) return false;
                }
                case ExileXCardsFromGraveyardCost ignored -> {
                    if (graveyard.isEmpty()) return false;
                }
                default -> { }
            }
        }
        return true;
    }
}
