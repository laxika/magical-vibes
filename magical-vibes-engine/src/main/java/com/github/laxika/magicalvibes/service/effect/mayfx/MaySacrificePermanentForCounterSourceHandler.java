package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SacrificedPermanentPower;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MaySacrificePermanentForCounterSourceEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.MaySacrificeForCounterSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles accepting or declining an optional sacrifice that puts a +1/+1 counter on its source. */
@Component
@RequiredArgsConstructor
public class MaySacrificePermanentForCounterSourceHandler implements MayEffectHandlerBean {

    private final MaySacrificeForCounterSupport maySacrificeForCounterSupport;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;

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
            if (matchingIds.size() == 1) {
                addCountersAfterSacrifice(gameData, controllerId, matchingIds.getFirst(), sourcePermanentId, effect);
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
            if (matchingIds.size() > 1) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.MaySacrificeForCounterOnSource(
                                controllerId, sourcePermanentId, ability.sourceCard(),
                                CounterType.PLUS_ONE_PLUS_ONE, effect.counterAmount()));
                playerInputService.beginPermanentChoice(gameData, controllerId, matchingIds,
                        "Choose " + effect.description() + " to sacrifice.");
                return;
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void addCountersAfterSacrifice(GameData gameData, UUID controllerId, UUID permanentId,
                                            UUID sourcePermanentId,
                                            MaySacrificePermanentForCounterSourceEffect effect) {
        if (effect.counterAmount() instanceof SacrificedPermanentPower) {
            maySacrificeForCounterSupport.sacrificeThenAddCountersEqualToPower(
                    gameData, controllerId, permanentId, sourcePermanentId,
                    CounterType.PLUS_ONE_PLUS_ONE);
        } else if (effect.counterAmount() instanceof Fixed fixed) {
            maySacrificeForCounterSupport.sacrificeThenAddCounters(
                    gameData, controllerId, permanentId, sourcePermanentId,
                    CounterType.PLUS_ONE_PLUS_ONE, fixed.value());
        } else {
            throw new IllegalArgumentException("Unsupported counter amount for may-sacrifice effect: "
                    + effect.counterAmount().getClass().getSimpleName());
        }
    }
}
