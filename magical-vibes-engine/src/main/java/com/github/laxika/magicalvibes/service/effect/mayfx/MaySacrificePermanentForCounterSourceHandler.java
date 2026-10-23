package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MaySacrificePermanentForCounterSourceEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.MaySacrificeForCounterSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/** Handles accepting or declining an optional sacrifice that puts a counter on its source. */
@Component
@RequiredArgsConstructor
public class MaySacrificePermanentForCounterSourceHandler implements MayEffectHandlerBean {

    private final MaySacrificeForCounterSupport maySacrificeForCounterSupport;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MaySacrificePermanentForCounterSourceEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        MaySacrificePermanentForCounterSourceEffect effect = ability.effects().stream()
                .filter(e -> e instanceof MaySacrificePermanentForCounterSourceEffect)
                .map(e -> (MaySacrificePermanentForCounterSourceEffect) e)
                .findFirst().orElseThrow();

        UUID controllerId = ability.controllerId();
        UUID sourcePermanentId = ability.sourcePermanentId();
        if (accepted) {
            List<UUID> matchingIds = maySacrificeForCounterSupport.matchingPermanentIds(
                    gameData, controllerId, sourcePermanentId, effect.filter());
            if (!matchingIds.isEmpty()) {
                var context = new PermanentChoiceContext.MaySacrificeForCounterOnSource(
                        controllerId, sourcePermanentId, ability.sourceCard(),
                        effect.counterType(), effect.counterAmount());
                if (matchingIds.size() == 1) {
                    permanentChoiceHandlerServiceProvider.getObject().completeLoneCandidateAskingDeathReplacementsFirst(
                            gameData, controllerId, matchingIds.getFirst(), context);
                    return;
                }
                gameData.interaction.setPermanentChoiceContext(context);
                playerInputService.beginPermanentChoice(gameData, controllerId, matchingIds,
                        "Choose " + effect.description() + " to sacrifice.");
                return;
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
