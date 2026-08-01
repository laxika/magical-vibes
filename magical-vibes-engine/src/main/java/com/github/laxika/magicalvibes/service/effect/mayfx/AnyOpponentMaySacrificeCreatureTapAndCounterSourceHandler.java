package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMaySacrificeCreatureTapAndCounterSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnyOpponentMaySacrificeCreatureTapAndCounterSourceEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Desecration Demon's combat trigger — one opponent's accept/decline. Accepting sacrifices a
 * creature of their choice (a pick is only asked for when they control more than one); either way
 * the next opponent that still controls a creature is offered the same choice.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnyOpponentMaySacrificeCreatureTapAndCounterSourceHandler implements MayEffectHandlerBean {

    private final AnyOpponentMaySacrificeCreatureTapAndCounterSourceEffectHandler effectHandler;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyOpponentMaySacrificeCreatureTapAndCounterSourceEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (AnyOpponentMaySacrificeCreatureTapAndCounterSourceEffect) ability.effects().getFirst();
        UUID chooserId = ability.controllerId();

        if (accepted) {
            List<UUID> creatures = effectHandler.creatureIds(gameData, chooserId);
            if (creatures.size() > 1) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.AnyOpponentSacrificeCreatureForTapAndCounter(
                                chooserId, ability.sourceCard(), effect));
                playerInputService.beginPermanentChoice(gameData, chooserId, creatures,
                        "Choose a creature to sacrifice.");
                return;
            }
            if (creatures.size() == 1) {
                log.info("Game {} - {} sacrifices a creature to {}", gameData.id, player.getUsername(),
                        ability.sourceCard().getName());
                effectHandler.sacrifice(gameData, chooserId, creatures.getFirst());
                effectHandler.advance(gameData, ability.sourceCard(), effect, chooserId, true);
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
            // Accepted but no creature left to sacrifice — treated as a decline.
        }

        effectHandler.advance(gameData, ability.sourceCard(), effect, chooserId, effect.anyAccepted());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
