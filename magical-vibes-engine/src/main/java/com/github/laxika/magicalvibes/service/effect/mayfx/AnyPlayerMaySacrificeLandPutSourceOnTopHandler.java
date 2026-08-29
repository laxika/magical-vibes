package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMaySacrificeLandPutSourceOnTopEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnyPlayerMaySacrificeLandPutSourceOnTopEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Handles accepting or declining one Argothian Wurm land-sacrifice choice. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnyPlayerMaySacrificeLandPutSourceOnTopHandler implements MayEffectHandlerBean {

    private final AnyPlayerMaySacrificeLandPutSourceOnTopEffectHandler effectHandler;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyPlayerMaySacrificeLandPutSourceOnTopEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (AnyPlayerMaySacrificeLandPutSourceOnTopEffect) ability.effects().getFirst();
        UUID chooserId = ability.controllerId();

        if (accepted) {
            List<UUID> lands = effectHandler.landIds(gameData, chooserId);
            if (lands.size() > 1) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.AnyPlayerMaySacrificeLandPutSourceOnTop(
                                chooserId, ability.sourceCard(), effect));
                playerInputService.beginPermanentChoice(
                        gameData, chooserId, lands, "Choose a land to sacrifice.");
                return;
            }
            if (lands.size() == 1) {
                effectHandler.sacrificeLand(gameData, chooserId, lands.getFirst());
                effectHandler.putSourceOnTop(gameData, ability.sourceCard(), effect);
                effectHandler.advance(gameData, ability.sourceCard(), effect, chooserId);
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
        }

        effectHandler.advance(gameData, ability.sourceCard(), effect, chooserId);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
