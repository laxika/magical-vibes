package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PlayersLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDrawsAndLosesLifePerChosenTypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the chosen-type creature count against the targeted player's battlefield. */
@Component
@RequiredArgsConstructor
public class TargetPlayerDrawsAndLosesLifePerChosenTypeEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final GameQueryService gameQueryService;
    private final DrawService drawService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerDrawsAndLosesLifePerChosenTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();

        if (gameData.chosenSpellSubtype == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellCreatureTypeChoice(gameData, controllerId);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        CardSubtype chosenSubtype = gameData.chosenSpellSubtype;
        gameData.chosenSpellSubtype = null;

        List<UUID> targetPlayerIds = entry.targetsForEffect(effect);
        if (targetPlayerIds.isEmpty() && entry.getTargetId() != null) {
            targetPlayerIds = List.of(entry.getTargetId());
        }

        PermanentAllOfPredicate creatureOfChosenType = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(chosenSubtype)));
        Map<UUID, Integer> losses = new LinkedHashMap<>();
        for (UUID targetPlayerId : targetPlayerIds) {
            if (!gameData.playerIds.contains(targetPlayerId)) {
                continue;
            }

            int count = gameQueryService.countControlledPermanentsMatching(
                    gameData, targetPlayerId, creatureOfChosenType);
            if (count <= 0) {
                continue;
            }
            // The draw is one instruction (so "two or more cards" replacements such as Alms Collector
            // apply), and the life loss follows it so a paused draw (e.g. a draw replacement choice)
            // finishes before the player loses life.
            drawService.resolveDrawCards(gameData, targetPlayerId, count);
            if (targetPlayerIds.size() == 1) {
                entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                        List.of(new LoseLifeEffect(count, LoseLifeRecipient.TARGET_PLAYER)));
            } else {
                losses.put(targetPlayerId, count);
            }
        }
        // Several target players lose their life together after the draws, as one step of its own.
        if (!losses.isEmpty()) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                    List.of(new PlayersLoseLifeEffect(losses)));
        }
    }
}
