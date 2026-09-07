package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutImprintedCreatureOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutImprintedCreatureOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutImprintedCreatureOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID controllerId = entry.getControllerId();
        Card imprintedCard = gameData.getImprintedCard(entry.getCard());
        String playerName = gameData.playerIdToName.get(controllerId);

        if (imprintedCard == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s imprint ability resolves but no card was imprinted."));
            return;
        }

        gameLogService.append(gameData, GameLog.textCardText(playerName + " turns the exiled card face up: " , imprintedCard, "."));

        boolean isCreature = imprintedCard.hasType(CardType.CREATURE);

        if (!isCreature) {
            gameLogService.append(gameData, GameLog.cardThen(imprintedCard, " is not a creature card. It remains in exile."));
            return;
        }

        // Remove from exile zone
        gameData.removeFromExile(imprintedCard.getId());

        // Put onto the battlefield
        Permanent perm = new Permanent(imprintedCard);
        perm.setEnteredFromExile(true);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);

        gameLogService.append(gameData, GameLog.entersBattlefieldUnder(imprintedCard, playerName));

        graveyardReturnSupport.handleCreatureEtbAndLegendRule(gameData, controllerId, perm, imprintedCard);

        log.info("Game {} - {} puts imprinted creature {} onto battlefield",
                gameData.id, playerName, imprintedCard.getName());
    }
}
