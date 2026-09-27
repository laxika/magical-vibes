package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreatureThenCreateTokenCopiesWithHalfPowerToughnessEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Saw in Half's destroy-and-copy effect. */
@Component
@RequiredArgsConstructor
public class DestroyTargetCreatureThenCreateTokenCopiesWithHalfPowerToughnessEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetCreatureThenCreateTokenCopiesWithHalfPowerToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        Card sourceCard = target.getCard();
        var targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        int power = gameQueryService.getEffectivePower(gameData, target);
        int toughness = gameQueryService.getEffectiveToughness(gameData, target);
        if (!destructionSupport.tryDestroyAndLog(gameData, target, entry.getCard().getName())) {
            return;
        }
        if (targetControllerId == null) {
            return;
        }

        CreateTokenCopyOfTargetPermanentEffect copyEffect = new CreateTokenCopyOfTargetPermanentEffect(
                java.util.List.of(), java.util.Set.of(),
                Math.floorDiv(power + 1, 2), Math.floorDiv(toughness + 1, 2), java.util.Map.of());
        tokenCopySupport.createTokenCopies(
                gameData, entry, Collections.nCopies(2, sourceCard), null, targetControllerId, copyEffect);
    }
}
