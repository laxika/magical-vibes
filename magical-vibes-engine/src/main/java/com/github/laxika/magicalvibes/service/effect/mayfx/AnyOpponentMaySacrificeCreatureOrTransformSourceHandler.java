package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AnyOpponentMaySacrificeCreatureOrTransformSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnyOpponentMaySacrificeCreatureOrTransformSourceEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/** Handles one opponent's accept/decline choice for Innocent Traveler's upkeep trigger. */
@Component
@RequiredArgsConstructor
public class AnyOpponentMaySacrificeCreatureOrTransformSourceHandler implements MayEffectHandlerBean {

    private final AnyOpponentMaySacrificeCreatureOrTransformSourceEffectHandler effectHandler;
    private final InputCompletionService inputCompletionService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;
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
                permanentChoiceHandlerServiceProvider.getObject().completeLoneCandidateAskingDeathReplacementsFirst(
                        gameData, chooserId, creatures.getFirst(),
                        new PermanentChoiceContext.AnyOpponentSacrificeCreatureForTransform(
                                chooserId, ability.sourceCard(), effect));
                return;
            }
            // Accepted but no creature remains legally available: treat it as a decline.
        }

        Runnable decline = () -> {
            effectHandler.advance(gameData, ability.sourceCard(), effect, chooserId, null);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        };
        if (effect.chosenCreatureIds().isEmpty()) {
            decline.run();
            return;
        }
        // The last opponent's decline sacrifices the earlier opponents' picks, so their death replacements come first.
        permanentChoiceHandlerServiceProvider.getObject().declineAskingDeathReplacementsFirst(
                gameData, player, ability, decline);
    }
}
