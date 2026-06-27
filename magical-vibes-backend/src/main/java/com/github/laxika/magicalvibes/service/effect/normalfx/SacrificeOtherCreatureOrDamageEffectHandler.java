package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreatureOrDamageEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SacrificeOtherCreatureOrDamageEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeOtherCreatureOrDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeOtherCreatureOrDamageEffect) effect;
        UUID controllerId = entry.getControllerId();
                String cardName = entry.getCard().getName();
                UUID sourceCardId = entry.getCard().getId();

                List<UUID> otherCreatureIds = destructionSupport.collectCreatureIds(gameData, controllerId,
                        p -> !p.getCard().getId().equals(sourceCardId));

                if (otherCreatureIds.isEmpty()) {
                    // Can't sacrifice — deal damage to controller
                    destructionSupport.dealNoncombatDamageToPlayer(gameData, controllerId, e.damage(), cardName, entry.getCard().getColor());
                    gameOutcomeService.checkWinCondition(gameData);
                    return;
                }

                if (otherCreatureIds.size() == 1) {
                    // Only one other creature — sacrifice it automatically
                    Permanent creature = gameQueryService.findPermanentById(gameData, otherCreatureIds.getFirst());
                    if (creature != null) {
                        destructionSupport.sacrificeAndLog(gameData, creature, controllerId);
                    }
                    return;
                }

                // Multiple other creatures — prompt player to choose
                gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.SacrificeCreature(controllerId));
                playerInputService.beginPermanentChoice(gameData, controllerId, otherCreatureIds,
                        "Choose a creature other than " + cardName + " to sacrifice.");
    }
}
