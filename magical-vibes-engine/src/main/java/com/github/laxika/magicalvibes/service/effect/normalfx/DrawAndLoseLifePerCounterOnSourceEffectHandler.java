package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawAndLoseLifePerCounterOnSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DrawAndLoseLifePerCounterOnSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawAndLoseLifePerCounterOnSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        if (source == null) {
            return;
        }

        int amount = amountEvaluationService.evaluate(gameData,
                ((DrawAndLoseLifePerCounterOnSourceEffect) effect).drawnCardAmount(),
                AmountContext.forStackEntry(entry, source));
        playerInteractionSupport.applyDrawCards(gameData, entry.getControllerId(), amount);
        // The life loss is a step of its own, so a death replacement question its Lich's Duel Mastery sacrifice
        // asks (CR 616.1f) reruns only the loss, not the draw.
        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                List.of(new LoseLifeEffect(amount, LoseLifeRecipient.CONTROLLER)));
    }
}
