package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetBasePowerToughnessEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetBasePowerToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SetBasePowerToughnessEffect) effect;
        // SELF scope ("this creature has base P/T X/Y until end of turn", e.g. Marsh Flitter)
        // resolves against the source; TARGET scope resolves against the chosen target.
        UUID id = e.scope() == GrantScope.SELF ? entry.getSourcePermanentId() : entry.getTargetId();
        Permanent target = gameQueryService.findPermanentById(gameData, id);
        if (target == null) {
            return;
        }

        target.setBasePowerToughnessOverriddenUntilEndOfTurn(true);
        target.setBasePowerOverride(e.power());
        target.setBaseToughnessOverride(e.toughness());

        String logEntry = target.getCard().getName() + " has base power and toughness " + e.power() + "/" + e.toughness() + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} base P/T set to {}/{}", gameData.id, target.getCard().getName(), e.power(), e.toughness());
    }
}
