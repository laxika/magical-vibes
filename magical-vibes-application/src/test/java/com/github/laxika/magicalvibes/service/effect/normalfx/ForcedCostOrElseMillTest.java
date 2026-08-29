package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerTestFixtures;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ForcedCostOrElseMillTest {

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
    @Mock
    private GraveyardService graveyardService;

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
                amountEvaluationService, graveyardService);
    }

    @Test
    void queuesChoiceWhenLibraryCanPay() {
        gameData.playerDecks.put(playerId, new ArrayList<>(List.of(card("Island"), card("Forest"))));
        ForcedCostOrElseEffect effect = effect(true);
        StackEntry entry = entry(effect);

        handler.resolve(gameData, entry, effect);

        assertThat(gameData.pendingMayAbilities).singleElement()
                .satisfies(ability -> assertThat(ability.effects()).containsExactly(effect));
    }

    @Test
    void millsWhenMandatoryCostCanBePaid() {
        gameData.playerDecks.put(playerId, new ArrayList<>(List.of(card("Island"), card("Forest"))));
        ForcedCostOrElseEffect effect = effect(false);
        StackEntry entry = entry(effect);

        handler.resolve(gameData, entry, effect);

        verify(graveyardService).resolveMillPlayer(gameData, playerId, 2);
        assertThat(gameData.pendingMayAbilities).isEmpty();
    }

    @Test
    void resolvesFallbackWhenLibraryCannotPay() {
        gameData.playerDecks.put(playerId, new ArrayList<>(List.of(card("Island"))));
        ForcedCostOrElseEffect effect = effect(true);
        StackEntry entry = entry(effect);

        handler.resolve(gameData, entry, effect);

        verify(destructionSupport).resolveForcedCostElseEffects(gameData, entry, effect);
        assertThat(gameData.pendingMayAbilities).isEmpty();
    }

    private ForcedCostOrElseEffect effect(boolean optional) {
        return new ForcedCostOrElseEffect(new MillControllerCost(2), List.of(new SacrificeSelfEffect()), optional);
    }

    private StackEntry entry(ForcedCostOrElseEffect effect) {
        Card source = EffectHandlerTestFixtures.createCard("Deep Spawn");
        return new StackEntry(StackEntryType.TRIGGERED_ABILITY, source, playerId,
                source.getName(), List.of(effect), null, UUID.randomUUID());
    }

    private Card card(String name) {
        return EffectHandlerTestFixtures.createCard(name);
    }
}
