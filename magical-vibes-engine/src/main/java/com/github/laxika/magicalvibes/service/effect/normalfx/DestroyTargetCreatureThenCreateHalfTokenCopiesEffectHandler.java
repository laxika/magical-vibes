package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreatureThenCreateHalfTokenCopiesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Resolves Saw in Half using the target's last-known characteristics and effective statistics. */
@Component
@RequiredArgsConstructor
public class DestroyTargetCreatureThenCreateHalfTokenCopiesEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetCreatureThenCreateHalfTokenCopiesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        Card targetCard = target.getCard();
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        int power = halfRoundedUp(gameQueryService.getEffectivePower(gameData, target));
        int toughness = halfRoundedUp(gameQueryService.getEffectiveToughness(gameData, target));
        if (!destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName())) {
            return;
        }

        CreateTokenCopyOfTargetPermanentEffect copyProfile = new CreateTokenCopyOfTargetPermanentEffect(
                List.of(), Set.of(), power, toughness, Map.of());
        tokenCopySupport.createTokenCopies(
                gameData, entry, List.of(targetCard, targetCard), null, targetControllerId, copyProfile);
    }

    private static int halfRoundedUp(int value) {
        return value >= 0 ? value / 2 + value % 2 : value / 2;
    }
}
