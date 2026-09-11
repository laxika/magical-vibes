package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TransmuteArtifactCardDispositionEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransmuteArtifactCardDispositionEffectHandler implements NormalEffectHandlerBean {

    private final PutCardFromHandOrGraveyardOntoBattlefieldSupport battlefieldSupport;
    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TransmuteArtifactCardDispositionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TransmuteArtifactCardDispositionEffect disposition =
                (TransmuteArtifactCardDispositionEffect) effect;
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) {
            return;
        }
        if (disposition.toBattlefield()) {
            battlefieldSupport.applyChoice(gameData, controllerId, disposition.cardId(),
                    entry.getCard().getName());
            return;
        }

        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null) {
            return;
        }
        Card selected = null;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getId().equals(disposition.cardId())) {
                selected = hand.remove(i);
                break;
            }
        }
        if (selected == null) {
            return;
        }
        UUID ownerId = selected.getOwnerId() == null ? controllerId : selected.getOwnerId();
        graveyardService.addCardToGraveyard(gameData, ownerId, selected, Zone.HAND);
        gameLogService.append(gameData, GameLog.cardThen(selected,
                " is put into its owner's graveyard."));
    }
}
