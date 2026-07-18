package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnAllCardsExiledWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnAllCardsExiledWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        List<ExiledCardEntry> toReturn = gameData.exiledCards.stream()
                .filter(e -> sourcePermanentId.equals(e.sourcePermanentId()))
                .toList();

        for (ExiledCardEntry exiledEntry : toReturn) {
            Card card = exiledEntry.card();
            UUID ownerId = exiledEntry.ownerId();
            if (!gameData.removeFromExile(card.getId())) {
                continue;
            }

            Permanent perm = new Permanent(card);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, perm);
            String logEntry = card.getName() + " returns to the battlefield under "
                    + gameData.playerIdToName.get(ownerId) + "'s control.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(card).text(" returns to the battlefield under " + gameData.playerIdToName.get(ownerId) + "'s control.").build());
            log.info("Game {} - {} returns from exile via {} (put into graveyard from battlefield)",
                    gameData.id, card.getName(), entry.getCard().getName());
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, ownerId, card, null, false);
        }
    }
}
