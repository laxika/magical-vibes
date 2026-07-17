package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.SacrificeAtEndStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreatureAndCreateSpiritCopyToSacrificeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DestroyTargetCreatureAndCreateSpiritCopyToSacrificeEffect} (Broken Visage):
 * capture the target's power/toughness, destroy it (can't be regenerated), then create a black
 * Spirit token with that power/toughness for the controller and schedule it for sacrifice at the
 * next end step.
 */
@Component
@RequiredArgsConstructor
public class DestroyTargetCreatureAndCreateSpiritCopyToSacrificeEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetCreatureAndCreateSpiritCopyToSacrificeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        // Capture power/toughness (last-known info) before the creature leaves the battlefield.
        int power = gameQueryService.getEffectivePower(gameData, target);
        int toughness = gameQueryService.getEffectiveToughness(gameData, target);

        destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName(), true);

        // The token is created regardless of whether the destruction succeeded.
        CreateTokenEffect spirit = new CreateTokenEffect("Spirit", power, toughness,
                CardColor.BLACK, List.of(CardSubtype.SPIRIT), Set.<Keyword>of(), Set.of());
        List<UUID> createdIds = permanentControlSupport.applyCreateToken(
                gameData, entry.getControllerId(), spirit, 1, entry.getCard().getSetCode());

        for (UUID id : createdIds) {
            gameData.queueDelayedAction(new SacrificeAtEndStep(id));
        }
    }
}
