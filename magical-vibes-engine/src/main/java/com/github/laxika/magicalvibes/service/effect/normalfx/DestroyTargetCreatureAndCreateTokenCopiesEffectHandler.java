package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreatureAndCreateTokenCopiesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Saw in Half's destroy-and-create-two-copies effect. */
@Component
@RequiredArgsConstructor
public class DestroyTargetCreatureAndCreateTokenCopiesEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetCreatureAndCreateTokenCopiesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        Card copiedCard = target.getCard();
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        int power = halfRoundedUp(gameQueryService.getEffectivePower(gameData, target));
        int toughness = halfRoundedUp(gameQueryService.getEffectiveToughness(gameData, target));

        boolean destroyed = destructionSupport.tryDestroyAndLog(
                gameData, target, entry.getCard().getName());
        if (!destroyed || targetControllerId == null) {
            return;
        }

        CreateTokenCopyOfTargetPermanentEffect copyProfile =
                new CreateTokenCopyOfTargetPermanentEffect(
                        List.of(), java.util.Set.of(), power, toughness, Map.of());
        tokenCopySupport.createTokenCopies(
                gameData, entry, List.of(copiedCard, copiedCard), null, targetControllerId, copyProfile);
    }

    private int halfRoundedUp(int value) {
        return Math.ceilDiv(value, 2);
    }
}
