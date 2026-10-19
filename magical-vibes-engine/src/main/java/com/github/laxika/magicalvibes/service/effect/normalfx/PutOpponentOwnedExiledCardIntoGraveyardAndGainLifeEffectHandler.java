package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutOpponentOwnedExiledCardIntoGraveyardAndGainLifeEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutOpponentOwnedExiledCardIntoGraveyardAndGainLifeEffectHandler
        implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutOpponentOwnedExiledCardIntoGraveyardAndGainLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutOpponentOwnedExiledCardIntoGraveyardAndGainLifeEffect) effect;
        UUID controllerId = entry.getControllerId();
        entry.setEventValue(0);
        List<UUID> validCardIds = new ArrayList<>();
        synchronized (gameData.exiledCards) {
            List<ExiledCardEntry> pileRepresentatives = new ArrayList<>();
            for (ExiledCardEntry exiled : gameData.exiledCards) {
                if (!gameData.playerIds.contains(exiled.ownerId()) || controllerId.equals(exiled.ownerId())) {
                    continue;
                }
                if (!exiled.faceDown()) {
                    validCardIds.add(exiled.card().getId());
                } else if (pileRepresentatives.stream().noneMatch(rep -> sameFaceDownPile(rep, exiled))) {
                    // Face-down cards are chosen as a pile, not individually; the pile is represented by
                    // its first card and a random card of that pile is the one that moves.
                    pileRepresentatives.add(exiled);
                    validCardIds.add(exiled.card().getId());
                }
            }
        }

        if (!validCardIds.isEmpty()) {
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.OpponentOwnedExiledCardToGraveyardChoice(
                            controllerId, validCardIds, e.lifeGain(), entry.getCard(), entry.getEntryType()));
        }
    }

    /**
     * Face-down exiled cards are grouped by what exiled them and when (2015-08-25 ruling); cards of
     * one group are indistinguishable to the chooser.
     */
    public static boolean sameFaceDownPile(ExiledCardEntry a, ExiledCardEntry b) {
        return a.faceDown() && b.faceDown()
                && java.util.Objects.equals(a.ownerId(), b.ownerId())
                && java.util.Objects.equals(a.sourcePermanentId(), b.sourcePermanentId())
                && java.util.Objects.equals(a.exilerId(), b.exilerId())
                && a.exiledTurnNumber() == b.exiledTurnNumber()
                && java.util.Objects.equals(a.abilityLink(), b.abilityLink());
    }
}
