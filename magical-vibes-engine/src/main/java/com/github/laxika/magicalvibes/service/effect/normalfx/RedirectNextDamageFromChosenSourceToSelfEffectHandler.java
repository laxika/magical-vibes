package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageFromChosenSourceToSelfEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedirectNextDamageFromChosenSourceToSelfEffectHandler implements NormalEffectHandlerBean {

    private final PreventionSupport preventionSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectNextDamageFromChosenSourceToSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID destinationId = entry.getSourcePermanentId();
        if (destinationId == null) {
            return;
        }

        List<UUID> validIds = new java.util.ArrayList<>(preventionSupport.collectAllBattlefieldPermanentIds(gameData));
        gameData.stack.stream()
                .filter(stackEntry -> stackEntry.getEntryType() == com.github.laxika.magicalvibes.model.StackEntryType.INSTANT_SPELL
                        || stackEntry.getEntryType() == com.github.laxika.magicalvibes.model.StackEntryType.SORCERY_SPELL)
                .map(stackEntry -> stackEntry.getCard().getId())
                .forEach(validIds::add);
        if (validIds.isEmpty()) {
            preventionSupport.broadcastNoPermanentsForDamageSourceChoice(gameData);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.RedirectNextDamageFromChosenSourceToPermanentChoice(
                        controllerId, destinationId));
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                "Choose a source. The next time it would deal damage this turn, that damage is dealt to "
                        + entry.getCard().getName() + " instead.");
    }
}
