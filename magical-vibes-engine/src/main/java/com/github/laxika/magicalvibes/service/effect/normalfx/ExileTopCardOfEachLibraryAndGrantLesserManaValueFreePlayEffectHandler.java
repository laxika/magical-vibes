package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachLibraryAndGrantLesserManaValueFreePlayEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardOfEachLibraryAndGrantLesserManaValueFreePlayEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfEachLibraryAndGrantLesserManaValueFreePlayEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<ExiledCardEntry> exiledByThisEffect = new ArrayList<>();

        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> library = gameData.playerDecks.get(playerId);
            if (library == null || library.isEmpty()) {
                continue;
            }

            Card card = library.removeFirst();
            exileService.exileCard(gameData, playerId, card);
            ExiledCardEntry exiledEntry = gameData.findExiledCard(card.getId());
            if (exiledEntry != null) {
                exiledByThisEffect.add(exiledEntry);
            }

            gameLogService.append(gameData, GameLog.builder()
                    .text(gameData.playerIdToName.get(playerId) + " exiles ")
                    .card(card)
                    .text(" from the top of their library (" + entry.getCard().getName() + ").")
                    .build());
        }

        Card referenceCard = exiledByThisEffect.stream()
                .filter(exiled -> controllerId.equals(exiled.ownerId()))
                .map(ExiledCardEntry::card)
                .findFirst()
                .orElse(null);
        if (referenceCard == null) {
            return;
        }

        int referenceManaValue = referenceCard.getManaValue();
        for (ExiledCardEntry exiled : exiledByThisEffect) {
            Card card = exiled.card();
            if (!card.getId().equals(referenceCard.getId())
                    && card.getManaValue() >= referenceManaValue) {
                continue;
            }

            gameData.exilePlayPermissions.put(card.getId(), controllerId);
            gameData.exilePlayPermissionsExpireEndOfTurn.add(card.getId());
            gameData.exilePlayWithoutPayingManaCost.add(card.getId());
        }

        log.info("Game {} - {} received Triple Triad free-play permissions for {} exiled cards",
                gameData.id, gameData.playerIdToName.get(controllerId),
                exiledByThisEffect.stream()
                        .filter(exiled -> exiled.card().getId().equals(referenceCard.getId())
                                || exiled.card().getManaValue() < referenceManaValue)
                        .count());
    }
}
