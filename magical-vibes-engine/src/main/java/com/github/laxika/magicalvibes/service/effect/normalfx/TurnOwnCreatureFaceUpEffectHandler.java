package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TurnOwnCreatureFaceUpEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TurnOwnCreatureFaceUpEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final ObjectProvider<GameService> gameServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TurnOwnCreatureFaceUpEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TurnOwnCreatureFaceUpEffect turnFaceUp = (TurnOwnCreatureFaceUpEffect) effect;
        List<UUID> eligibleIds = eligibleIds(gameData, entry.getControllerId(), turnFaceUp.creaturesOnly());
        if (eligibleIds.isEmpty()) {
            return;
        }
        if (eligibleIds.size() == 1) {
            turnFaceUp(gameData, eligibleIds.getFirst(), turnFaceUp.creaturesOnly());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.TurnOwnCreatureFaceUp(turnFaceUp.creaturesOnly()));
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), eligibleIds,
                turnFaceUp.creaturesOnly()
                        ? "Choose a creature you control to turn face up."
                        : "Choose a face-down permanent you control to turn face up.");
    }

    public void completeChoice(GameData gameData, UUID chosenPermanentId, boolean creaturesOnly) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("No effect is waiting for a creature choice");
        }

        Permanent target = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (target == null
                || !target.isFaceDown()
                || (creaturesOnly && !gameQueryService.isCreature(gameData, target))
                || !entry.getControllerId().equals(
                        gameQueryService.findPermanentController(gameData, chosenPermanentId))) {
            throw new IllegalStateException("Choose a face-down permanent you control");
        }
        turnFaceUp(gameData, target, creaturesOnly);
    }

    private List<UUID> eligibleIds(GameData gameData, UUID controllerId, boolean creaturesOnly) {
        return gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> permanent.isFaceDown()
                        && (!creaturesOnly || gameQueryService.isCreature(gameData, permanent)))
                .filter(permanent -> !isInstantOrSorceryFaceDownCard(permanent))
                .map(Permanent::getId)
                .toList();
    }

    /**
     * A cloaked permanent represented by an instant or sorcery card is revealed and left face down
     * when it would turn face up (CR 701.58g), and so is a manifested one (CR 701.40g), so neither is ever
     * a turn-face-up target.
     */
    private boolean isInstantOrSorceryFaceDownCard(Permanent permanent) {
        return (permanent.isCloaked() || permanent.isManifested())
                && (permanent.getCard().hasType(CardType.INSTANT) || permanent.getCard().hasType(CardType.SORCERY));
    }

    private void turnFaceUp(GameData gameData, UUID permanentId, boolean creaturesOnly) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (permanent != null) {
            turnFaceUp(gameData, permanent, creaturesOnly);
        }
    }

    private void turnFaceUp(GameData gameData, Permanent permanent, boolean creaturesOnly) {
        if (creaturesOnly) {
            gameServiceProvider.getObject().turnPermanentFaceUpWithoutPayingManaCost(gameData, permanent);
        } else {
            gameServiceProvider.getObject().turnPermanentFaceUpWithoutPayingManaCostOfAnyType(gameData, permanent);
        }
    }
}
