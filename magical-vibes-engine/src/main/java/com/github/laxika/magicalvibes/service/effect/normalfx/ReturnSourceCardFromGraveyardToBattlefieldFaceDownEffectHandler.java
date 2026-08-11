package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldFaceDownEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;

import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnSourceCardFromGraveyardToBattlefieldFaceDownEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSourceCardFromGraveyardToBattlefieldFaceDownEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card card = entry.getCard();
        UUID graveyardOwnerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        if (graveyardOwnerId == null) {
            log.info("Game {} - {} face-down graveyard return fizzles (no longer in a graveyard)",
                    gameData.id, card.getName());
            return;
        }

        if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)) {
            gameLogService.append(gameData,
                    GameLog.cardThen(card, " can't return from the graveyard; it stays in the graveyard."));
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        Permanent permanent = new Permanent(card);
        permanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        permanent.setEnteredFromGraveyardOwnerId(graveyardOwnerId);
        battlefieldEntryService.putPermanentOntoBattlefield(
                gameData, entry.getControllerId(), permanent, enterTappedTypes);

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        gameLogService.append(gameData,
                GameLog.text(playerName + " returns a card to the battlefield face down."));
        log.info("Game {} - {} returns face down to the battlefield under {}'s control",
                gameData.id, card.getName(), playerName);
    }
}
