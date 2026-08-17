package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerTestFixtures;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ForcedCostOrElseEnergyTest {

    @Mock
    private DestructionSupport destructionSupport;
    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private PredicateEvaluationService predicateEvaluationService;
    @Mock
    private PlayerInputService playerInputService;
    @Mock
    private LibraryExileSupport libraryExileSupport;
    @Mock
    private DrawService drawService;
    @Mock
    private AmountEvaluationService amountEvaluationService;

    private GameData gameData;
    private UUID playerId;
    private ForcedCostOrElseEffectHandler handler;

    @BeforeEach
    void setUp() {
        var game = EffectHandlerTestFixtures.newTwoPlayerGameDataFull();
        gameData = game.gameData();
        playerId = game.player1Id();
        handler = new ForcedCostOrElseEffectHandler(
                destructionSupport, gameQueryService, predicateEvaluationService,
                playerInputService, libraryExileSupport, null, null, null, drawService,
                amountEvaluationService, null);
    }

    @Test
    void queuesPaymentChoiceWhenEnergyIsAvailable() {
        gameData.playerEnergyCounters.put(playerId, 2);
        ForcedCostOrElseEffect effect = effect();
        StackEntry entry = entry(effect);

        handler.resolve(gameData, entry, effect);

        assertThat(gameData.pendingMayAbilities).singleElement()
                .satisfies(ability -> {
                    assertThat(ability.controllerId()).isEqualTo(playerId);
                    assertThat(ability.effects()).containsExactly(effect);
                });
    }

    @Test
    void resolvesFallbackWhenEnergyIsUnavailable() {
        gameData.playerEnergyCounters.put(playerId, 1);
        ForcedCostOrElseEffect effect = effect();
        StackEntry entry = entry(effect);

        handler.resolve(gameData, entry, effect);

        verify(destructionSupport).resolveForcedCostElseEffects(gameData, entry, effect);
        assertThat(gameData.pendingMayAbilities).isEmpty();
    }

    private ForcedCostOrElseEffect effect() {
        return new ForcedCostOrElseEffect(new PayEnergyCost(2), List.of(new SacrificeSelfEffect()), true);
    }

    private StackEntry entry(ForcedCostOrElseEffect effect) {
        Card source = EffectHandlerTestFixtures.createCard("Lathnu Hellion");
        return new StackEntry(StackEntryType.TRIGGERED_ABILITY, source, playerId,
                source.getName(), List.of(effect), null, UUID.randomUUID());
    }
}
