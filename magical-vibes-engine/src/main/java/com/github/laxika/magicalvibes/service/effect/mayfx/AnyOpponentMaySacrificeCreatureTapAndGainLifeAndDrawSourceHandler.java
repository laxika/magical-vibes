package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMaySacrificeCreatureTapAndGainLifeAndDrawSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnyOpponentMaySacrificeCreatureTapAndGainLifeAndDrawSourceEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/** Handles one opponent's accept/decline choice for Clackbridge Troll. */
@Component
@RequiredArgsConstructor
public class AnyOpponentMaySacrificeCreatureTapAndGainLifeAndDrawSourceHandler
        implements MayEffectHandlerBean {

    private final AnyOpponentMaySacrificeCreatureTapAndGainLifeAndDrawSourceEffectHandler effectHandler;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyOpponentMaySacrificeCreatureTapAndGainLifeAndDrawSourceEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (AnyOpponentMaySacrificeCreatureTapAndGainLifeAndDrawSourceEffect) ability.effects().getFirst();
        UUID chooserId = ability.controllerId();

        if (accepted) {
            List<UUID> creatures = effectHandler.creatureIds(gameData, chooserId);
            if (creatures.size() > 1) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.AnyOpponentSacrificeCreatureForTapAndGainLifeAndDraw(
                                chooserId, ability.sourceCard(), effect));
                playerInputService.beginPermanentChoice(gameData, chooserId, creatures,
                        "Choose a creature to sacrifice.");
                return;
            }
            if (creatures.size() == 1) {
                permanentChoiceHandlerServiceProvider.getObject().completeLoneCandidateAskingDeathReplacementsFirst(
                        gameData, chooserId, creatures.getFirst(),
                        new PermanentChoiceContext.AnyOpponentSacrificeCreatureForTapAndGainLifeAndDraw(
                                chooserId, ability.sourceCard(), effect));
                return;
            }
        }

        effectHandler.advance(gameData, ability.sourceCard(), effect, chooserId, effect.anyAccepted());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
