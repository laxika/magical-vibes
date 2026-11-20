package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;


@Slf4j
@Component
@RequiredArgsConstructor
public class GrantProtectionFromColorUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantProtectionFromColorUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantProtectionFromColorUntilEndOfTurnEffect) effect;
        if (e.scope() == GrantScope.OWN_CREATURES) {
            // The one-shot "creatures you control" includes the source.
            subjectResolutionService.permanents(gameData, entry, effect, GrantScope.ALL_OWN_CREATURES.subject())
                    .forEach(permanent -> grantProtection(entry, permanent, e.color(), gameData));
            return;
        }

        Permanent target = subjectResolutionService.permanent(gameData, entry, effect,
                e.scope() == GrantScope.SELF ? GrantScope.SELF.subject() : GrantScope.TARGET.subject());
        if (target == null) {
            return;
        }

        grantProtection(entry, target, e.color(), gameData);
    }

    private void grantProtection(StackEntry entry, Permanent target, CardColor color, GameData gameData) {
        gameData.addEffectOnPermanent(entry, new ProtectionFromColorsEffect(Set.of(color), GrantScope.TARGET),
                target.getId(), Duration.UNTIL_END_OF_TURN);

        String colorName = color.name().toLowerCase();
        gameLogService.append(gameData, GameLog.builder()
                .card(target.getCard())
                .text(" gains protection from " + colorName + " until end of turn.")
                .build());

        log.info("Game {} - {} gains protection from {} until end of turn", gameData.id, target.getCard().getName(), colorName);
    }
}
