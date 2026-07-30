package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CreatureDamageRedirectShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageToTargetCreatureToSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedirectNextDamageToTargetCreatureToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectNextDamageToTargetCreatureToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RedirectNextDamageToTargetCreatureToSourceEffect) effect;
        UUID redirectTargetId = entry.getSourcePermanentId();
        UUID protectedPermanentId = entry.getTargetId();
        // Without the source creature (the redirect destination) or a legal protected target,
        // the ability does nothing.
        if (protectedPermanentId == null || redirectTargetId == null) return;
        Permanent source = gameQueryService.findPermanentById(gameData, redirectTargetId);
        Permanent protectedPerm = gameQueryService.findPermanentById(gameData, protectedPermanentId);
        if (source == null || protectedPerm == null) return;

        int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source));
        if (amount <= 0) return;

        // Any-source (null), amount-limited redirect shield protecting the targeted creature.
        gameData.creatureDamageRedirectShields.add(new CreatureDamageRedirectShield(
                protectedPermanentId, null, amount, redirectTargetId));

        gameLogService.append(gameData, GameLog.builder()
                .text("The next " + amount + " damage that would be dealt this turn to ")
                .card(protectedPerm.getCard())
                .text(" is dealt to ")
                .card(source.getCard())
                .text(" instead.")
                .build());
        log.info("Game {} - registered next-{}-damage redirect from {} to {}", gameData.id, amount,
                protectedPerm.getCard().getName(), source.getCard().getName());
    }
}
