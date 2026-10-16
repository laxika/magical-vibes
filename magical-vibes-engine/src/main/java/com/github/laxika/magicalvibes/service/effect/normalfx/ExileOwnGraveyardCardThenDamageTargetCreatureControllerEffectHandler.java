package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnGraveyardCardThenDamageTargetCreatureControllerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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
    private final GameLogService gameLogService;
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

        if (e.exiled()) {
            dealDamageToTargetControllerOf(gameData, entry, e.damage(), sourceName);
            return;
        }

        // "If you do" — with no card to exile, the exile can't happen, so no damage.
        if (candidates.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + " has no cards in graveyard to exile (" + sourceName + ")."));
            return;
        }

        // Exile a card from the controller's graveyard. Auto-exile when only one candidate;
        // otherwise the player chooses which card to exile and the damage is queued behind that
        // choice, so it is only dealt once a card has actually been exiled.
        if (candidates.size() == 1) {
            Card card = candidates.getFirst();
            graveyard.remove(card);
            graveyardService.notifyCardsExiledFromGraveyard(gameData, controllerId, card);
            exileService.exileCard(gameData, controllerId, card);
            gameLogService.append(gameData, GameLog.builder().text(playerName + " exiles ").card(card).text(" from their graveyard (" + sourceName + ").").build());
            dealDamageToTargetControllerOf(gameData, entry, e.damage(), sourceName);
        } else {
            graveyardReturnSupport.beginGraveyardExileChoice(gameData, controllerId, 1, sourceCard,
                    List.of(new ExileOwnGraveyardCardThenDamageTargetCreatureControllerEffect(e.damage(), true)));
        }
    }

    private void dealDamageToTargetControllerOf(GameData gameData, StackEntry entry, int damage, String sourceName) {
        // Within a single spell resolution the target creature is still on the battlefield even
        // if it took lethal damage earlier, so its controller is available via last-known info.
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (gameQueryService.isDamageFromStackEntryPrevented(gameData, entry)) {
            gameLogService.append(gameData, GameLog.text(sourceName + "'s damage to " + gameData.playerIdToName.get(targetControllerId) + " is prevented."));
        } else {
            damageSupport.dealDamageToPlayer(gameData, entry, targetControllerId, damage);
        }
        gameOutcomeService.checkWinCondition(gameData);
    }
}
