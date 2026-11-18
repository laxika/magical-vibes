package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseSubtypesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.duration.DurationClock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoseSubtypesUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final DurationClock durationClock;
    private final GameLogService gameLogService;
    private final UnattachTriggerSupport unattachTriggerSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseSubtypesUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var lose = (LoseSubtypesUntilEndOfTurnEffect) effect;
        Permanent self = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (self == null) {
            return;
        }
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                entry.getCard().getName(), entry.getSourcePermanentId(), entry.getControllerId(), lose,
                self.getId(), null, null, Duration.UNTIL_END_OF_TURN, 0));

        // CR 704.5p — a permanent that is no longer an Equipment/Aura/Fortification cannot stay
        // attached. Unattach immediately so subsequent effects in the same resolution see it free.
        if (self.isAttached() && lose.subtypes().contains(CardSubtype.EQUIPMENT)
                && !gameQueryService.hasEffectiveSubtype(gameData, self, CardSubtype.EQUIPMENT)
                && !gameQueryService.hasEffectiveSubtype(gameData, self, CardSubtype.AURA)) {
            unattachTriggerSupport.triggerDestroyOnUnattachIfNeeded(gameData, self, self.getAttachedTo());
            self.setAttachedTo(null);
            durationClock.sourceUnattached(gameData, self.getId());
            gameLogService.append(gameData, GameLog.cardThen(self.getCard(), " becomes unattached."));
        }

        String names = lose.subtypes().stream().map(CardSubtype::getDisplayName)
                .reduce((a, b) -> a + ", " + b).orElse("");
        gameLogService.append(gameData, GameLog.cardThen(self.getCard(),
                " is no longer " + names + " until end of turn."));
    }
}
