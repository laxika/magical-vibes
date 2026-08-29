package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromExileToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnSourceCardFromExileToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSourceCardFromExileToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var returnEffect = (ReturnSourceCardFromExileToBattlefieldEffect) effect;
        Card card = entry.getCard();
        ExiledCardEntry exiledEntry = gameData.findExiledCard(card.getId());
        if (exiledEntry == null) {
            log.info("Game {} - {} exile return fizzles (no longer in exile)", gameData.id, card.getName());
            return;
        }

        if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, Zone.EXILE)) {
            gameLogService.append(gameData, GameLog.cardThen(card,
                    " can't return from exile; it stays in exile."));
            log.info("Game {} - {} exile return blocked", gameData.id, card.getName());
            return;
        }

        UUID ownerId = exiledEntry.ownerId();
        gameData.removeFromExile(card.getId());

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        Permanent permanent = new Permanent(card);
        if (returnEffect.tapped()) {
            permanent.tap();
        }
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, permanent, enterTappedTypes);

        String playerName = gameData.playerIdToName.get(ownerId);
        gameLogService.append(gameData, GameLog.textCardText(playerName + " returns ", card,
                " to the battlefield from exile" + (returnEffect.tapped() ? " tapped" : "") + "."));
        log.info("Game {} - {} returns to the battlefield from exile", gameData.id, card.getName());

        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, ownerId, card, null, false);
    }
}
