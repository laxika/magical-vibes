package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.GameContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.PassPriorityRequest;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.*;

class AiGameActionsSubgameTest {
    @Test
    void actionsFollowTheActiveGameAndRejectDecisionsFromEarlierActivations() {
        Player player = new Player(UUID.randomUUID(), "AI");
        GameData root = new GameData(UUID.randomUUID(), "Game", player.getId(), player.getUsername());
        GameData child = new GameData(UUID.randomUUID(), "Game", player.getId(), player.getUsername());
        root.status = child.status = GameStatus.RUNNING;
        GameRegistry registry = new GameRegistry();
        registry.register(root);
        GameService service = mock(GameService.class);
        AiGameActions actions = new AiGameActions(root.id, player, service, registry);
        GameContext original = root.session.context();
        actions.beginDecision(original);

        root.session.push(child);
        actions.handlePassPriority(new PassPriorityRequest());
        verifyNoInteractions(service);
        actions.beginDecision(root.session.context());
        actions.handlePassPriority(new PassPriorityRequest());
        verify(service).passPriority(child, player);

        root.session.pop();
        actions.beginDecision(original);
        actions.handlePassPriority(new PassPriorityRequest());
        verify(service, never()).passPriority(root, player);
        actions.beginDecision(root.session.context());
        actions.handlePassPriority(new PassPriorityRequest());
        verify(service).passPriority(root, player);
    }
}
