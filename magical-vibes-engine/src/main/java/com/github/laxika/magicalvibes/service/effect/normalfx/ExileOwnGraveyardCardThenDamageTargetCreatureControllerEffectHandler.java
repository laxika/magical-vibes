package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnGraveyardCardThenDamageTargetCreatureControllerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileOwnGraveyardCardThenDamageTargetCreatureControllerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final ExileService exileService;
    private final GraveyardService graveyardService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileOwnGraveyardCardThenDamageTargetCreatureControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileOwnGraveyardCardThenDamageTargetCreatureControllerEffect) effect;

        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        String sourceName = entry.getCard().getName();

        // The resolving spell may already have been placed into its owner's graveyard during the
        // "you may" pause; per the rules it is still resolving and not actually in the graveyard,
        // so it is not a legal card to exile. Exclude it from the candidates.
        Card sourceCard = entry.getCard();
        List<Card> candidates = graveyard == null ? List.of()
                : graveyard.stream().filter(c -> c != sourceCard).toList();

        // "If you do" — with no card to exile, the exile can't happen, so no damage.
        if (candidates.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " has no cards in graveyard to exile (" + sourceName + ")."));
            return;
        }

        // The exile is happening, so deal the damage to the target creature's controller.
        // (Within a single spell resolution the target creature is still on the battlefield even
        // if it took lethal damage earlier, so its controller is available via last-known info.)
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target != null) {
            UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
            if (gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(sourceName + "'s damage to " + gameData.playerIdToName.get(targetControllerId) + " is prevented."));
            } else {
                int rawDamage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), entry);
                damageSupport.dealDamageToPlayer(gameData, entry, targetControllerId, rawDamage);
            }
            gameOutcomeService.checkWinCondition(gameData);
        }

        // Exile a card from the controller's graveyard. Auto-exile when only one candidate,
        // otherwise prompt the player to choose which card to exile.
        if (candidates.size() == 1) {
            Card card = candidates.getFirst();
            graveyard.remove(card);
            graveyardService.notifyCardsLeftGraveyard(gameData, controllerId);
            exileService.exileCard(gameData, controllerId, card);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " exiles " + card.getName() + " from their graveyard (" + sourceName + ")."));
        } else {
            graveyardReturnSupport.beginGraveyardExileChoice(gameData, controllerId, 1, sourceCard);
        }
    }
}
