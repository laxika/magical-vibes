package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MaySacrificePermanentForCounterOrTapSourceEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.MaySacrificeForCounterSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Accept/decline half of "you may sacrifice [a permanent]. If you do, put a +1/+1 counter on this
 * creature. If you don't, tap this creature." (Ravenous Vampire). Accepting with several legal
 * permanents starts a choice; declining — or no longer controlling anything to sacrifice — taps the
 * source.
 */
@Component
@RequiredArgsConstructor
public class MaySacrificePermanentForCounterOrTapSourceHandler implements MayEffectHandlerBean {

    private final MaySacrificeForCounterSupport maySacrificeForCounterSupport;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MaySacrificePermanentForCounterOrTapSourceEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        MaySacrificePermanentForCounterOrTapSourceEffect effect = ability.effects().stream()
                .filter(e -> e instanceof MaySacrificePermanentForCounterOrTapSourceEffect)
                .map(e -> (MaySacrificePermanentForCounterOrTapSourceEffect) e)
                .findFirst().orElseThrow();

        UUID controllerId = ability.controllerId();
        UUID sourcePermanentId = ability.sourcePermanentId();

        if (accepted) {
            List<UUID> matchingIds = maySacrificeForCounterSupport.matchingPermanentIds(
                    gameData, controllerId, sourcePermanentId, effect.filter());
            if (matchingIds.size() == 1) {
                maySacrificeForCounterSupport.sacrificeThenAddCounter(
                        gameData, controllerId, matchingIds.getFirst(), sourcePermanentId);
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
            if (matchingIds.size() > 1) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.MaySacrificeForCounterOnSource(
                                controllerId, sourcePermanentId, ability.sourceCard()));
                playerInputService.beginPermanentChoice(gameData, controllerId, matchingIds,
                        "Choose " + effect.description() + " to sacrifice.");
                return;
            }
            // Accepted but nothing left to sacrifice — the source taps.
        }

        maySacrificeForCounterSupport.tapSource(gameData, sourcePermanentId);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
