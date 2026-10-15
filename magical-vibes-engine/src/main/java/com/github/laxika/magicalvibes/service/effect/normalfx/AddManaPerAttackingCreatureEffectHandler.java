package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
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

        // "Any combination" — each mana gets its own color, so distribute them one at a time
        interactionHandlerRegistry.begin(gameData, new ChoiceContext.PersistentManaDistributionChoice(
                controllerId, attackerCount, List.of(e.color1(), e.color2())).prompt());

        String playerName = gameData.playerIdToName.get(controllerId);
        log.info("Game {} - Awaiting {} to distribute {} mana among {} and {}",
                gameData.id, playerName, attackerCount, e.color1(), e.color2());
    }
}
