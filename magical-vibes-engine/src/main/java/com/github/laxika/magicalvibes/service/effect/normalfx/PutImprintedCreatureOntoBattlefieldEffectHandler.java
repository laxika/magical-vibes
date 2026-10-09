package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
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
import java.util.ArrayList;
import java.util.List;

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
        String playerName = gameData.playerIdToName.get(controllerId);
        List<ExiledCardEntry> linkedCards = gameData.exiledCards.stream()
                .filter(exiled -> entry.getSourcePermanentId() != null
                        && entry.getSourcePermanentId().equals(exiled.sourcePermanentId()))
                .toList();
        Card legacyImprinted = gameData.getImprintedCard(entry.getCard());
        if (linkedCards.isEmpty() && legacyImprinted != null) {
            ExiledCardEntry legacyEntry = gameData.findExiledCard(legacyImprinted.getId());
            if (legacyEntry != null && legacyEntry.sourcePermanentId() == null) {
                linkedCards = List.of(legacyEntry);
            }
        }

        if (linkedCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s imprint ability resolves but no card was imprinted."));
            return;
        }

        List<Permanent> entering = new ArrayList<>();
        for (ExiledCardEntry exiled : linkedCards) {
            Card imprintedCard = exiled.card();
            gameData.exiledCards.replaceAll(card -> card.card().getId().equals(imprintedCard.getId())
                    ? new ExiledCardEntry(card.card(), card.ownerId(), card.sourcePermanentId(), false,
                            card.exilerId(), card.exiledTurnNumber(), card.controllerTurnsTakenAtExile(), card.abilityLink())
                    : card);
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " turns the exiled card face up: ", imprintedCard, "."));
            if (!imprintedCard.hasType(CardType.CREATURE)) {
                gameLogService.append(gameData, GameLog.cardThen(imprintedCard,
                        " is not a creature card. It remains in exile."));
                continue;
            }
            if (!gameData.removeFromExile(imprintedCard.getId())) continue;
            Permanent permanent = new Permanent(imprintedCard);
            permanent.setEnteredFromExile(true);
            entering.add(permanent);
        }
        var enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        for (Permanent permanent : entering) {
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, controllerId, permanent, enterTappedTypes, entering);
            gameLogService.append(gameData, GameLog.entersBattlefieldUnder(permanent.getCard(), playerName));
        }
        for (Permanent permanent : entering) {
            graveyardReturnSupport.handleCreatureEtbAndLegendRule(
                    gameData, controllerId, permanent, permanent.getCard());
        }
    }
}
