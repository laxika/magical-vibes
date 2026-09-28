package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.VillainousChoiceState;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCreatureThenOwnerFacesVillainousChoiceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves This Is How It Ends's target-owner villainous choice. */
@Component
@RequiredArgsConstructor
@Slf4j
public class ShuffleTargetCreatureThenOwnerFacesVillainousChoiceEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LifeSupport lifeSupport;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final VillainousChoiceSupport villainousChoiceSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleTargetCreatureThenOwnerFacesVillainousChoiceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        VillainousChoiceState state = gameData.villainousChoice;
        if (!state.active) {
            Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
            if (target == null) {
                return;
            }

            UUID ownerId = gameData.defaultControllerOf(target.getId());
            if (!shuffleIntoLibrary(gameData, target, entry.getCard().getName())) {
                return;
            }

            state.reset();
            state.active = true;
            state.currentTargetId = ownerId;
        }

        UUID ownerId = state.currentTargetId;
        if (ownerId == null || !gameData.playerIds.contains(ownerId)) {
            finish(gameData);
            return;
        }

        if (state.chosenMode != null) {
            String chosen = state.chosenMode;
            state.chosenMode = null;
            if (ShuffleTargetCreatureThenOwnerFacesVillainousChoiceEffect.LOSE_LIFE_OPTION.equals(chosen)) {
                lifeSupport.applyLifeLoss(gameData, ownerId, 5, entry.getCard().getName());
                finish(gameData);
            } else {
                beginCreatureChoice(gameData, entry, ownerId);
            }
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = true;
        villainousChoiceSupport.beginChoice(gameData, ownerId, entry.getCard().getName(),
                ShuffleTargetCreatureThenOwnerFacesVillainousChoiceEffect.SHUFFLE_CREATURE_OPTION,
                List.of(ShuffleTargetCreatureThenOwnerFacesVillainousChoiceEffect.LOSE_LIFE_OPTION,
                        ShuffleTargetCreatureThenOwnerFacesVillainousChoiceEffect.SHUFFLE_CREATURE_OPTION),
                entry.getCard().getName() + " — Choose a villainous choice.");
    }

    private void beginCreatureChoice(GameData gameData, StackEntry entry, UUID ownerId) {
        List<UUID> creatureIds = collectOwnedCreatureIds(gameData, ownerId);
        if (creatureIds.isEmpty()) {
            finish(gameData);
            return;
        }

        if (creatureIds.size() == 1) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
            if (creature != null) {
                shuffleIntoLibrary(gameData, creature, entry.getCard().getName());
            }
            finish(gameData);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.DestroyChosenCreature(
                ownerId, entry.getCard().getName(), false, false, null, null, true));
        playerInputService.beginPermanentChoice(gameData, ownerId, creatureIds,
                "Choose another creature you own to shuffle into your library.");
    }

    private List<UUID> collectOwnedCreatureIds(GameData gameData, UUID ownerId) {
        List<UUID> ids = new ArrayList<>();
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (ownerId.equals(gameData.defaultControllerOf(permanent.getId()))
                        && gameQueryService.isCreature(gameData, permanent)) {
                    ids.add(permanent.getId());
                }
            }
        }
        return ids;
    }

    private boolean shuffleIntoLibrary(GameData gameData, Permanent permanent, String sourceCardName) {
        String name = permanent.getCard().getName();
        boolean removed = permanentRemovalService.removePermanentToLibraryShuffled(gameData, permanent);
        if (removed) {
            gameLogService.append(gameData, GameLog.text(name + " is shuffled into its owner's library."));
            log.info("Game {} - {} shuffled {} into its owner's library", gameData.id, sourceCardName, name);
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
        return removed;
    }

    private void finish(GameData gameData) {
        if (villainousChoiceSupport.repeatIfNeeded(gameData)) {
            return;
        }
        gameData.villainousChoice.reset();
        gameData.rerunCurrentEffectAfterInteraction = false;
    }
}
