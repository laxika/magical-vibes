package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeAllColorsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeChosenColorsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeAllColorsUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeAllColorsUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
        if (self == null) {
            return;
        }

        // CR 613 layer engine: "becomes all colors until end of turn" is a floating layer-5
        // color-setting effect with its own timestamp. Reuse the BecomeChosenColors setter carrying
        // every color; the legacy transient fields are seeded for direct getEffectiveColor callers.
        Set<CardColor> allColors = EnumSet.allOf(CardColor.class);
        self.getTransientColors().clear();
        self.getTransientColors().addAll(allColors);
        self.setColorOverridden(true);
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                entry.getCard().getName(), null, entry.getControllerId(),
                new BecomeChosenColorsUntilEndOfTurnEffect(allColors),
                self.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));

        String logEntry = self.getCard().getName() + " becomes all colors until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} becomes all colors until end of turn", gameData.id, self.getCard().getName());
    }
}
