package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeTargetAnteCardWithTopOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Darkpact's exchange with a card in the ante. */
@Component
@RequiredArgsConstructor
public class ExchangeTargetAnteCardWithTopOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExchangeTargetAnteCardWithTopOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (entry.getTargetZone() != Zone.EXILE || entry.getTargetId() == null) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription() + " fizzles (no valid ante target)."));
            return;
        }

        ExiledCardEntry anteEntry = gameData.findExiledCard(entry.getTargetId());
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (anteEntry == null || !gameData.antedCardIds.contains(entry.getTargetId())
                || !controllerId.equals(anteEntry.ownerId())) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription() + " fizzles (the target is no longer a card you own in the ante)."));
            return;
        }
        if (library == null) {
            return;
        }

        Card anteCard = anteEntry.card();
        gameData.removeFromExile(anteCard.getId());
        if (library.isEmpty()) {
            library.addFirst(anteCard);
            gameLogService.append(gameData, GameLog.builder()
                    .card(anteCard)
                    .text(" is moved from the ante to the empty library of ")
                    .text(gameData.playerIdToName.get(controllerId))
                    .text(" by ")
                    .card(entry.getCard())
                    .text(".")
                    .build());
            return;
        }

        Card libraryTop = library.removeFirst();
        library.addFirst(anteCard);
        gameData.addToAnte(controllerId, libraryTop);

        gameLogService.append(gameData, GameLog.builder()
                .card(anteCard)
                .text(" is exchanged with the top card of ")
                .card(entry.getCard())
                .text("'s library by ")
                .text(gameData.playerIdToName.get(controllerId))
                .text(".")
                .build());
    }
}
