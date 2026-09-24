package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentDrawAndUntapEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Intellectual Offering's resolution-time opponent choices. */
@Component
@RequiredArgsConstructor
public class ChooseOpponentDrawAndUntapEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseOpponentDrawAndUntapEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choiceEffect = (ChooseOpponentDrawAndUntapEffect) effect;
        List<UUID> opponents = opponentsOf(gameData, entry.getControllerId());
        if (opponents.isEmpty()) {
            return;
        }

        var context = new PermanentChoiceContext.ChooseOpponentDrawAndUntap(
                entry.getControllerId(), choiceEffect.untapChoice(), entry.getCard().getName());
        if (opponents.size() == 1) {
            continueResolution(gameData, entry, entry.getEffectsToResolve().indexOf(effect) + 1,
                    choiceEffect.untapChoice(), opponents.getFirst());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(context);
        playerInputService.beginPlayerChoice(gameData, entry.getControllerId(), opponents,
                entry.getCard().getName() + " — choose an opponent.");
    }

    public void completeChoice(GameData gameData, UUID chosenOpponentId,
                               PermanentChoiceContext.ChooseOpponentDrawAndUntap context) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null || !opponentsOf(gameData, context.controllerId()).contains(chosenOpponentId)) {
            return;
        }

        continueResolution(gameData, entry, gameData.pendingEffectResolutionIndex,
                context.untapChoice(), chosenOpponentId);
    }

    private void continueResolution(GameData gameData, StackEntry entry, int insertionIndex,
                                     boolean untapChoice, UUID chosenOpponentId) {
        entry.setTargetId(chosenOpponentId);
        if (untapChoice) {
            var nonland = new PermanentNotPredicate(new PermanentIsLandPredicate());
            entry.insertEffectsToResolve(insertionIndex, List.of(
                    new UntapPermanentsEffect(TapUntapScope.CONTROLLED, nonland),
                    new UntapPermanentsEffect(TapUntapScope.TARGET_PLAYERS_PERMANENTS, nonland)));
        } else {
            entry.insertEffectsToResolve(insertionIndex, List.of(
                    new DrawCardEffect(3),
                    new DrawCardForTargetPlayerEffect(3),
                    new ChooseOpponentDrawAndUntapEffect(true)));
        }
    }

    private List<UUID> opponentsOf(GameData gameData, UUID controllerId) {
        return gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .toList();
    }
}
