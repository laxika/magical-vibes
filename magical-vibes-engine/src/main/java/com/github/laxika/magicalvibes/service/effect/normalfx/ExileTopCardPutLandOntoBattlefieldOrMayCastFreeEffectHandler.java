package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardPutLandOntoBattlefieldOrMayCastFreeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardPutLandOntoBattlefieldOrMayCastFreeEffectHandler
        implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardPutLandOntoBattlefieldOrMayCastFreeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        if (library == null || library.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(playerName + "'s library is empty — nothing to exile."));
            return;
        }

        Card topCard = library.removeFirst();
        exileService.exileCard(gameData, controllerId, topCard);
        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " exiles ")
                .card(topCard)
                .text(" from the top of their library.")
                .build());

        if (topCard.hasType(CardType.LAND)) {
            gameData.removeFromExile(topCard.getId());
            Permanent permanent = new Permanent(topCard);
            permanent.setEnteredFromExile(true);
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, controllerId, permanent);
            battlefieldEntryService.processLandETBEffects(gameData, controllerId, topCard);
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                "Cast " + topCard.getName() + " without paying its mana cost?",
                topCard.getId()));
        log.info("Game {} - {} may cast {} without paying its mana cost",
                gameData.id, playerName, topCard.getName());
    }
}
