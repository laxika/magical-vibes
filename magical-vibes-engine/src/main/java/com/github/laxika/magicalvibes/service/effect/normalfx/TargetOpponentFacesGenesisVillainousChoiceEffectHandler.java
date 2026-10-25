package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.VillainousChoiceState;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentFacesGenesisVillainousChoiceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Resolves Genesis of the Daleks's targeted villainous choice. */
@Component
@RequiredArgsConstructor
public class TargetOpponentFacesGenesisVillainousChoiceEffectHandler implements NormalEffectHandlerBean {

    /** The progress mark of the step resolved again after the Dalek choice's life loss step. */
    private static final String AFTER_LIFE_LOSS = "__after_life_loss";

    private static final PermanentAllOfPredicate DALEK_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(), new PermanentHasSubtypePredicate(CardSubtype.DALEK)));
    private static final PermanentAllOfPredicate NON_DALEK_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(), new PermanentNotPredicate(
                    new PermanentHasSubtypePredicate(CardSubtype.DALEK))));

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final DestroyAllPermanentsEffectHandler destroyAllPermanentsEffectHandler;
    private final VillainousChoiceSupport villainousChoiceSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetOpponentFacesGenesisVillainousChoiceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        VillainousChoiceState state = gameData.villainousChoice;
        if (!state.active) {
            state.reset();
            state.active = true;
            state.currentTargetId = entry.getTargetId();
        }

        if (state.chosenMode != null) {
            String chosen = state.chosenMode;
            state.chosenMode = null;
            if (AFTER_LIFE_LOSS.equals(chosen)) {
                finish(gameData);
                return;
            }
            if (TargetOpponentFacesGenesisVillainousChoiceEffect.DESTROY_DALEKS.equals(chosen)) {
                Map<UUID, Integer> losses = resolveDalekChoice(gameData, entry);
                if (!losses.isEmpty()) {
                    // The losses follow the destruction as a step of their own, so the Lich's Duel Mastery
                    // sacrifices that replace them ask their CR 616.1 questions before any life is lost without
                    // destroying again; this step is resolved again after it to finish (or repeat) the choice.
                    state.chosenMode = AFTER_LIFE_LOSS;
                    gameData.rerunCurrentEffectAfterInteraction = false;
                    entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                            List.of(new PlayersLoseLifeEffect(losses), effect));
                    return;
                }
            } else {
                destroyAllPermanentsEffectHandler.resolve(gameData, entry,
                        new DestroyAllPermanentsEffect(NON_DALEK_CREATURE));
            }
            finish(gameData);
            return;
        }

        UUID targetPlayerId = state.currentTargetId;
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)
                || targetPlayerId.equals(entry.getControllerId())) {
            state.reset();
            gameData.rerunCurrentEffectAfterInteraction = false;
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = true;
        villainousChoiceSupport.beginChoice(gameData, targetPlayerId, entry.getCard().getName(),
                TargetOpponentFacesGenesisVillainousChoiceEffect.DESTROY_DALEKS,
                List.of(TargetOpponentFacesGenesisVillainousChoiceEffect.DESTROY_DALEKS,
                        TargetOpponentFacesGenesisVillainousChoiceEffect.DESTROY_NON_DALEKS),
                entry.getCard().getName() + " — Choose a villainous choice.");
    }

    private void finish(GameData gameData) {
        if (villainousChoiceSupport.repeatIfNeeded(gameData)) {
            return;
        }
        gameData.villainousChoice.reset();
        gameData.rerunCurrentEffectAfterInteraction = false;
    }

    /** Destroys every Dalek creature and returns the life each opponent then loses (CR 608.2h: fixed now). */
    private Map<UUID, Integer> resolveDalekChoice(GameData gameData, StackEntry entry) {
        destroyAllPermanentsEffectHandler.resolve(gameData, entry,
                new DestroyAllPermanentsEffect(DALEK_CREATURE));

        int totalPower = gameData.creatureSubtypeDeathPowerThisTurn.values().stream()
                .mapToInt(subtypePowers -> subtypePowers.getOrDefault(CardSubtype.DALEK, 0))
                .sum();
        Map<UUID, Integer> losses = new LinkedHashMap<>();
        if (totalPower <= 0) {
            return losses;
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(entry.getControllerId()) && gameData.playerIds.contains(playerId)) {
                losses.put(playerId, totalPower);
            }
        }
        return losses;
    }
}
