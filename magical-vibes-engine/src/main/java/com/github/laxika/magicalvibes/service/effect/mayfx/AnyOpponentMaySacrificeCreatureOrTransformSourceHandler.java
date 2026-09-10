package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMaySacrificeCreatureOrTransformSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnyOpponentMaySacrificeCreatureOrTransformSourceEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Handles one opponent's accept/decline choice for Innocent Traveler's upkeep trigger. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnyOpponentMaySacrificeCreatureOrTransformSourceHandler implements MayEffectHandlerBean {

    private final AnyOpponentMaySacrificeCreatureOrTransformSourceEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyOpponentMaySacrificeCreatureOrTransformSourceEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (AnyOpponentMaySacrificeCreatureOrTransformSourceEffect) ability.effects().getFirst();
        UUID chooserId = ability.controllerId();

        if (accepted) {
            List<UUID> creatures = effectHandler.creatureIds(
                    gameData, chooserId, effect.abilityControllerId());
            if (creatures.size() > 1) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.AnyOpponentSacrificeCreatureForTransform(
                                chooserId, ability.sourceCard(), effect));
                playerInputService.beginPermanentChoice(gameData, chooserId, creatures,
                        "Choose a creature to sacrifice.");
                return;
            }
            if (creatures.size() == 1) {
                log.info("Game {} - {} chooses a creature to sacrifice to {}", gameData.id,
                        player.getUsername(), ability.sourceCard().getName());
                effectHandler.advance(gameData, ability.sourceCard(), effect, chooserId, creatures.getFirst());
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
            // Accepted but no creature remains legally available: treat it as a decline.
        }

        effectHandler.advance(gameData, ability.sourceCard(), effect, chooserId, null);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
