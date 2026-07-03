package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AddManaPerAttackingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddManaPerAttackingCreatureEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AddManaPerAttackingCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AddManaPerAttackingCreatureEffect) effect;

        UUID controllerId = entry.getControllerId();

        // Attacker count is locked at trigger time via xValue (per MTG rules: creatures
        // removed before resolution still count, tokens entering attacking after don't)
        int attackerCount = entry.getXValue();
        if (attackerCount == 0) {
            return;
        }

        // Present color choice: player picks one of the two offered colors, all mana is added as that color
        ChoiceContext.AttackManaSplitChoice choiceContext =
                new ChoiceContext.AttackManaSplitChoice(controllerId, attackerCount);
        List<String> colors = List.of(e.color1().name(), e.color2().name());
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null, choiceContext, colors,
                "Choose a color of mana to add (" + attackerCount + " mana)."));

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to choose mana color for {} attacking creatures",
                gameData.id, playerName, attackerCount);
    
    }
}
