package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CreatureControllerDamageRedirectShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectAllDamageToChosenCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedirectAllDamageToChosenCreatureUntilNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectAllDamageToChosenCreatureUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        var creatures = gameData.playerBattlefields.getOrDefault(controllerId, java.util.List.of())
                .stream().filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .map(Permanent::getId).toList();
        if (creatures.isEmpty()) return;
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.RedirectCreatureDamage(controllerId));
        playerInputService.beginPermanentChoice(gameData, controllerId, creatures,
                "Choose a creature you control to receive redirected damage.");
    }

    public void completeChoice(GameData gameData, UUID controllerId, UUID targetId) {
        Permanent target = targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || controllerId == null
                || !controllerId.equals(gameQueryService.findPermanentController(gameData, targetId))
                || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        gameData.creatureControllerDamageRedirectShields.add(
                new CreatureControllerDamageRedirectShield(controllerId, targetId));
    }
}
