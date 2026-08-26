package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMaySacrificeCreatureToCounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnyPlayerMaySacrificeCreatureToCounterSpellEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles one player's Brain Gorgers creature-sacrifice choice. */
@Component
@RequiredArgsConstructor
public class AnyPlayerMaySacrificeCreatureToCounterSpellHandler implements MayEffectHandlerBean {

    private final AnyPlayerMaySacrificeCreatureToCounterSpellEffectHandler effectHandler;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyPlayerMaySacrificeCreatureToCounterSpellEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (AnyPlayerMaySacrificeCreatureToCounterSpellEffect) ability.effects().getFirst();
        UUID chooserId = ability.controllerId();

        if (accepted) {
            List<UUID> creatures = effectHandler.creatureIds(gameData, chooserId);
            if (creatures.size() > 1) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.AnyPlayerMaySacrificeCreatureToCounterSpell(
                                chooserId, ability.sourceCard(), effect));
                playerInputService.beginPermanentChoice(
                        gameData, chooserId, creatures, "Choose a creature to sacrifice.");
                return;
            }
            if (creatures.size() == 1) {
                effectHandler.sacrificeCreature(gameData, chooserId, creatures.getFirst());
                effectHandler.counterSpell(gameData, ability.sourceCard(), effect);
                effectHandler.advance(gameData, ability.sourceCard(), effect, chooserId, true);
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
                return;
            }
        }

        effectHandler.advance(gameData, ability.sourceCard(), effect, chooserId, effect.anyAccepted());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
