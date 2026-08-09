package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MaySacrificePermanentForCounterOrRemoveSourceCounterEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.MaySacrificeForCounterSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Accept/decline handler for a sacrifice-for-counter upkeep choice with a counter-removal fallback. */
@Component
@RequiredArgsConstructor
public class MaySacrificePermanentForCounterOrRemoveSourceCounterHandler implements MayEffectHandlerBean {

    private final MaySacrificeForCounterSupport maySacrificeForCounterSupport;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MaySacrificePermanentForCounterOrRemoveSourceCounterEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        MaySacrificePermanentForCounterOrRemoveSourceCounterEffect effect = ability.effects().stream()
                .filter(e -> e instanceof MaySacrificePermanentForCounterOrRemoveSourceCounterEffect)
                .map(e -> (MaySacrificePermanentForCounterOrRemoveSourceCounterEffect) e)
                .findFirst().orElseThrow();

        UUID controllerId = ability.controllerId();
        UUID sourcePermanentId = ability.sourcePermanentId();
        if (accepted) {
            List<UUID> matchingIds = maySacrificeForCounterSupport.matchingPermanentIds(
                    gameData, controllerId, effect.filter());
            if (matchingIds.size() == 1) {
                maySacrificeForCounterSupport.sacrificeThenAddCounter(
                        gameData, controllerId, matchingIds.getFirst(), sourcePermanentId, effect.counterType());
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
            if (matchingIds.size() > 1) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.MaySacrificeForCounterOnSource(
                                controllerId, sourcePermanentId, ability.sourceCard(), effect.counterType()));
                playerInputService.beginPermanentChoice(gameData, controllerId, matchingIds,
                        "Choose " + effect.description() + " to sacrifice.");
                return;
            }
        }

        maySacrificeForCounterSupport.removeCounterFromSource(
                gameData, sourcePermanentId, effect.counterType());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
