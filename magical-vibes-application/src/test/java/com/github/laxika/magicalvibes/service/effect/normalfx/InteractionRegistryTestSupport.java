package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.WarpWorldService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.HandTopBottomChoiceInteractionHandler;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.interaction.LibraryReorderInteractionHandler;
import com.github.laxika.magicalvibes.service.interaction.ScryInteractionHandler;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;

import static org.mockito.Mockito.mock;

/**
 * Builds an {@link InteractionHandlerRegistry} with real prompt/answer handlers (backed by the
 * given mocks) for unit tests that exercise effect handlers beginning interactions.
 * Continuation services (auto-pass, effect resumption, Warp World) are mocked out.
 */
final class InteractionRegistryTestSupport {

    private InteractionRegistryTestSupport() {
    }

    static InteractionHandlerRegistry registryFor(SessionManager sessionManager,
                                                  CardViewFactory cardViewFactory,
                                                  GameBroadcastService gameBroadcastService) {
        InteractionHandlerRegistry registry = new InteractionHandlerRegistry();
        registry.register(new LibraryReorderInteractionHandler(
                sessionManager, cardViewFactory, gameBroadcastService,
                mock(WarpWorldService.class), mock(PlayerInputService.class),
                mock(TurnProgressionService.class), mock(EffectResolutionService.class)));
        registry.register(new HandTopBottomChoiceInteractionHandler(
                sessionManager, cardViewFactory, gameBroadcastService, mock(TurnProgressionService.class)));
        registry.register(new ScryInteractionHandler(
                sessionManager, cardViewFactory, gameBroadcastService,
                mock(PlayerInputService.class), mock(TurnProgressionService.class),
                mock(EffectResolutionService.class)));
        return registry;
    }
}
