package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnExiledCardToBattlefieldUnderOwnerControlEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Returns one named exiled card to the battlefield under its <em>owner's</em> control — not the
 * controller of the trigger, which is why this is not
 * {@link ReturnCardExiledWithSourceToBattlefieldEffectHandler}.
 *
 * <p>The card id is baked in when the token carrying this death trigger is created, so a card that
 * already left exile (or never reached it) simply does nothing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnExiledCardToBattlefieldUnderOwnerControlEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnExiledCardToBattlefieldUnderOwnerControlEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID cardId = ((ReturnExiledCardToBattlefieldUnderOwnerControlEffect) effect).exiledCardId();
        if (cardId == null) {
            return;
        }
        ExiledCardEntry exiled = gameData.findExiledCard(cardId);
        if (exiled == null || !gameData.removeFromExile(cardId)) {
            return;
        }
        Card card = exiled.card();
        UUID ownerId = exiled.ownerId();
        Permanent permanent = new Permanent(card);
        permanent.setEnteredFromExile(true);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, permanent);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(ownerId) + " returns ", card,
                " from exile to the battlefield."));
        log.info("Game {} - {} returns from exile to the battlefield under its owner's control",
                gameData.id, card.getName());
        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, ownerId, card, null, false);
    }
}
