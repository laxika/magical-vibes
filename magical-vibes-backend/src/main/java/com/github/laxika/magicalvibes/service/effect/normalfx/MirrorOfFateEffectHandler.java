package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MirrorOfFateEffect;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MirrorOfFateEffectHandler implements NormalEffectHandlerBean {

    private final ExileSupport exileSupport;
    private final PlayerInputService playerInputService;
    private final CardViewFactory cardViewFactory;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MirrorOfFateEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);

        List<Card> exiledCards = gameData.getPlayerExiledCards(controllerId);
        if (exiledCards.isEmpty()) {
            // No exiled cards to choose — just exile the entire library
            exileSupport.exileLibraryAndPutChosenOnTop(gameData, controllerId, List.of());
            return;
        }

        // Present up to 7 face-up exiled cards the player owns for selection
        int maxCount = Math.min(7, exiledCards.size());
        List<UUID> validCardIds = exiledCards.stream().map(Card::getId).toList();
        List<CardView> cardViews = exiledCards.stream().map(cardViewFactory::create).toList();

        gameData.interaction.beginMirrorOfFateChoice(controllerId, new HashSet<>(validCardIds), maxCount);
        playerInputService.sendMirrorOfFateChoice(gameData, controllerId, validCardIds, cardViews, maxCount);

        log.info("Game {} - Awaiting {} to choose exiled cards for Mirror of Fate (up to {})",
                gameData.id, controllerName, maxCount);
    }
}
