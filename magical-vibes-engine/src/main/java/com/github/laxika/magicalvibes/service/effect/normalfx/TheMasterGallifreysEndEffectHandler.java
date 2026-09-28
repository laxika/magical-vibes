package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.VillainousChoiceState;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TheMasterGallifreysEndEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves The Master's death-triggered exile and villainous choice. */
@Component
@RequiredArgsConstructor
public class TheMasterGallifreysEndEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final LifeSupport lifeSupport;
    private final TokenCopySupport tokenCopySupport;
    private final VillainousChoiceSupport villainousChoiceSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TheMasterGallifreysEndEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TheMasterGallifreysEndEffect typed = (TheMasterGallifreysEndEffect) effect;
        VillainousChoiceState state = gameData.villainousChoice;

        if (!state.active) {
            state.reset();
            state.active = true;
            if (!exileDyingCard(gameData, typed.dyingCardId())) {
                finish(gameData);
                return;
            }

            List<UUID> opponents = opponents(gameData, entry.getControllerId());
            if (opponents.isEmpty()) {
                finish(gameData);
                return;
            }

            int highestLife = opponents.stream().mapToInt(gameData::getLife).max().orElse(Integer.MIN_VALUE);
            List<UUID> tiedOpponents = opponents.stream()
                    .filter(playerId -> gameData.getLife(playerId) == highestLife)
                    .toList();
            if (tiedOpponents.size() > 1) {
                state.waitingForCardChoice = true;
                gameData.rerunCurrentEffectAfterInteraction = true;
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.TheMasterMostLifeChoice(
                                entry.getCard(), entry.getControllerId(), tiedOpponents));
                playerInputService.beginPlayerChoice(gameData, entry.getControllerId(), tiedOpponents,
                        entry.getCard().getName() + " — Choose an opponent tied for most life.");
                return;
            }
            state.currentTargetId = tiedOpponents.getFirst();
        }

        if (state.waitingForCardChoice) {
            return;
        }

        if (state.chosenMode != null) {
            String chosen = state.chosenMode;
            state.chosenMode = null;
            if (TheMasterGallifreysEndEffect.LOSE_LIFE_OPTION.equals(chosen)) {
                if (state.currentTargetId != null && gameData.playerIds.contains(state.currentTargetId)) {
                    lifeSupport.applyLifeLoss(gameData, state.currentTargetId, 4, entry.getCard().getName());
                }
            } else if (TheMasterGallifreysEndEffect.CREATE_TOKEN_OPTION.equals(chosen)) {
                ExiledCardEntry exiled = gameData.findExiledCard(typed.dyingCardId());
                if (exiled != null) {
                    tokenCopySupport.createTokenCopies(gameData, entry, List.of(exiled.card()), null,
                            entry.getControllerId(), new CreateTokenCopyOfTargetPermanentEffect());
                }
            }
            finish(gameData);
            return;
        }

        UUID targetPlayerId = state.currentTargetId;
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)
                || targetPlayerId.equals(entry.getControllerId())) {
            finish(gameData);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = true;
        villainousChoiceSupport.beginChoice(gameData, targetPlayerId, entry.getCard().getName(),
                TheMasterGallifreysEndEffect.CREATE_TOKEN_OPTION,
                List.of(TheMasterGallifreysEndEffect.LOSE_LIFE_OPTION,
                        TheMasterGallifreysEndEffect.CREATE_TOKEN_OPTION),
                entry.getCard().getName() + " — Choose a villainous choice.");
    }

    public void completeMostLifeChoice(GameData gameData, UUID chosenPlayerId,
                                       PermanentChoiceContext.TheMasterMostLifeChoice context) {
        VillainousChoiceState state = gameData.villainousChoice;
        if (!context.eligiblePlayerIds().contains(chosenPlayerId)) {
            return;
        }
        state.currentTargetId = chosenPlayerId;
        state.waitingForCardChoice = false;
    }

    private boolean exileDyingCard(GameData gameData, UUID dyingCardId) {
        if (dyingCardId == null) {
            return false;
        }
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCardId);
        var dyingCard = gameQueryService.findCardInGraveyardById(gameData, dyingCardId);
        if (ownerId == null || dyingCard == null) {
            return false;
        }
        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, dyingCardId);
        exileService.exileCard(gameData, ownerId, dyingCard);
        return gameData.findExiledCard(dyingCardId) != null;
    }

    private List<UUID> opponents(GameData gameData, UUID controllerId) {
        return gameData.orderedPlayerIds.stream()
                .filter(gameData.playerIds::contains)
                .filter(playerId -> !playerId.equals(controllerId))
                .toList();
    }

    private void finish(GameData gameData) {
        if (villainousChoiceSupport.repeatIfNeeded(gameData)) {
            return;
        }
        gameData.villainousChoice.reset();
        gameData.rerunCurrentEffectAfterInteraction = false;
    }
}
