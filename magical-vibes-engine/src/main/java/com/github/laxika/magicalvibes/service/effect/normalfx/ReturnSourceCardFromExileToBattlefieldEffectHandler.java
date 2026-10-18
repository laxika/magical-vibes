package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromExileToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Returns a source card from exile to the battlefield under its owner's control. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnSourceCardFromExileToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    @Autowired
    @Lazy
    private StepTriggerService stepTriggerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSourceCardFromExileToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ReturnSourceCardFromExileToBattlefieldEffect returnEffect =
                (ReturnSourceCardFromExileToBattlefieldEffect) effect;
        UUID cardId = entry.getCard().getId();
        ExiledCardEntry exiled = gameData.findExiledCard(cardId);
        if (exiled == null) {
            return;
        }

        Card card = exiled.card();
        if (returnEffect.attacking()) {
            // CR 508.4: a permanent put onto the battlefield attacking is attacking whichever
            // player or planeswalker its controller chooses, not necessarily what another attacker attacks.
            stepTriggerService.resolvePendingExileReturn(gameData, new PendingExileReturn(
                    card, exiled.ownerId(), returnEffect.tapped(), false, TurnStep.DECLARE_BLOCKERS, 0,
                    List.of(), false, false, true, false, null, null, false, false, 0, Set.of(), null, 0,
                    Map.of(), false, false, null, null));
            return;
        }
        if (!gameData.removeFromExile(cardId)) {
            return;
        }
        Permanent permanent = new Permanent(card);
        if (returnEffect.tapped()) {
            permanent.tap();
        }
        UUID ownerId = exiled.ownerId();
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, permanent);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(ownerId) + " returns ", card,
                " from exile to the battlefield" + (returnEffect.tapped() ? " tapped" : "")
                        + "."));
        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, ownerId, card, null, false);
        log.info("Game {} - {} returns from exile to the battlefield under its owner's control",
                gameData.id, card.getName());
    }
}
