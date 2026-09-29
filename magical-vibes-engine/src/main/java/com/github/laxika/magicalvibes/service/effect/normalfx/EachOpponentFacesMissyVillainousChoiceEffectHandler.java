package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.VillainousChoiceState;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesDealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentFacesMissyVillainousChoiceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EachOpponentFacesMissyVillainousChoiceEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final ControlledCreaturesDealDamageToTargetPlayerEffectHandler damageHandler;
    private final PlanechaseService planechaseService;
    private final VillainousChoiceSupport villainousChoiceSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentFacesMissyVillainousChoiceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        VillainousChoiceState state = gameData.villainousChoice;
        if (!state.active) {
            state.reset();
            state.active = true;
            state.remaining.addAll(EachPlayerMayScryEffectHandler
                    .apnapOpponents(gameData, entry.getControllerId()));
        }

        if (state.waitingForDraw) {
            state.waitingForDraw = false;
            planechaseService.chaos(gameData);
            advance(gameData, entry);
            return;
        }
        if (state.chosenMode != null) {
            String chosen = state.chosenMode;
            state.chosenMode = null;
            if (EachOpponentFacesMissyVillainousChoiceEffect.DAMAGE_OPTION.equals(chosen)) {
                dealDamage(gameData, entry, state.currentPlayerId);
            } else {
                drawAndChaos(gameData, entry);
            }
            return;
        }

        advance(gameData, entry);
    }

    private void advance(GameData gameData, StackEntry entry) {
        VillainousChoiceState state = gameData.villainousChoice;
        if (villainousChoiceSupport.repeatIfNeeded(gameData)) {
            return;
        }
        UUID opponentId = state.remaining.pollFirst();
        if (opponentId == null) {
            state.reset();
            gameData.rerunCurrentEffectAfterInteraction = false;
            return;
        }
        state.currentPlayerId = opponentId;
        gameData.rerunCurrentEffectAfterInteraction = true;
        villainousChoiceSupport.beginChoice(gameData, opponentId, entry.getCard().getName(),
                EachOpponentFacesMissyVillainousChoiceEffect.DAMAGE_OPTION,
                List.of(EachOpponentFacesMissyVillainousChoiceEffect.DAMAGE_OPTION,
                        EachOpponentFacesMissyVillainousChoiceEffect.DRAW_OPTION),
                entry.getCard().getName() + " — Choose a villainous choice.");
    }

    private void dealDamage(GameData gameData, StackEntry entry, UUID opponentId) {
        if (opponentId != null && gameData.playerIds.contains(opponentId)) {
            StackEntry damageEntry = new StackEntry(
                    entry.getEntryType(), entry.getCard(), entry.getControllerId(),
                    entry.getDescription(), List.of(), opponentId, entry.getSourcePermanentId());
            damageHandler.resolve(gameData, damageEntry,
                    new ControlledCreaturesDealDamageToTargetPlayerEffect(1,
                            new PermanentAllOfPredicate(List.of(
                                    new PermanentIsCreaturePredicate(),
                                    new PermanentIsArtifactPredicate()))));
        }
        advance(gameData, entry);
    }

    private void drawAndChaos(GameData gameData, StackEntry entry) {
        VillainousChoiceState state = gameData.villainousChoice;
        state.waitingForDraw = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInteractionSupport.applyDrawCards(gameData, entry.getControllerId(), 1);
        if (!gameData.interaction.isAwaitingInput() && gameData.pendingMayAbilities.isEmpty()) {
            state.waitingForDraw = false;
            planechaseService.chaos(gameData);
            advance(gameData, entry);
        }
    }
}
