package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMaySacrificeNontokenCreatureTapAndSeekLandSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnyOpponentMaySacrificeNontokenCreatureTapAndSeekLandSourceEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Handles Gitrog, Horror of Zhava's accept/decline choice. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnyOpponentMaySacrificeNontokenCreatureTapAndSeekLandSourceHandler
        implements MayEffectHandlerBean {

    private final AnyOpponentMaySacrificeNontokenCreatureTapAndSeekLandSourceEffectHandler effectHandler;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyOpponentMaySacrificeNontokenCreatureTapAndSeekLandSourceEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (AnyOpponentMaySacrificeNontokenCreatureTapAndSeekLandSourceEffect)
                ability.effects().getFirst();
        UUID chooserId = ability.controllerId();

        if (accepted) {
            List<UUID> creatures = effectHandler.creatureIds(gameData, chooserId);
            if (creatures.size() > 1) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.AnyOpponentSacrificeNontokenCreatureForTapAndSeekLand(
                                chooserId, ability.sourceCard(), effect));
                playerInputService.beginPermanentChoice(gameData, chooserId, creatures,
                        "Choose a nontoken creature to sacrifice.");
                return;
            }
            if (creatures.size() == 1) {
                log.info("Game {} - {} sacrifices a nontoken creature to {}", gameData.id,
                        player.getUsername(), ability.sourceCard().getName());
                effectHandler.sacrifice(gameData, chooserId, creatures.getFirst());
                effectHandler.advance(gameData, ability.sourceCard(), effect, chooserId, true);
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
        }

        effectHandler.advance(gameData, ability.sourceCard(), effect, chooserId, effect.anyAccepted());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
