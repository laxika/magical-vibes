package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import org.mockito.Mockito;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

/**
 * Installs a realistic answer for the mocked {@link GameBroadcastService#isCardPlayable}
 * behind the AI's engine-backed castability check: plain affordability against the passed
 * pool plus the test's stubbed cast-cost modifier, and the creature-mana requirement. Keeps
 * the mock-wired AI unit tests (cost modifiers, creature mana, X spells) meaningful without
 * pulling in the full engine playability stack; harness-based tests use the real engine.
 */
final class AiTestPlayabilityStub {

    private AiTestPlayabilityStub() {
    }

    static void install(GameBroadcastService gameBroadcastService, CastingCostService castingCostService) {
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
}
