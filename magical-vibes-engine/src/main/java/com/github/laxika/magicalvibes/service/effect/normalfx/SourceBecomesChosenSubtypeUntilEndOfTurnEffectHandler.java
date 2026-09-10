package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.SourceBecomesChosenSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SourceBecomesChosenSubtypeUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SourceBecomesChosenSubtypeUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.chosenSpellSubtype == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellCreatureTypeChoice(gameData, entry.getControllerId(), Set.of());
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        CardSubtype chosenSubtype = gameData.chosenSpellSubtype;
        gameData.chosenSpellSubtype = null;

        UUID sourceId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null) {
            return;
        }

        var typeChange = (SourceBecomesChosenSubtypeUntilEndOfTurnEffect) effect;
        if (typeChange.retainOtherTypes()) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                    entry.getCard().getName(), source.getId(), entry.getControllerId(),
                    new GrantSubtypeEffect(chosenSubtype, GrantScope.TARGET),
                    source.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
        } else {
            source.setTransientCreatureTypeOverride(chosenSubtype);
            source.getTransientCreatureTypeOverrides().clear();
        }
        gameLogService.append(gameData, GameLog.builder()
                .card(source.getCard())
                .text(" becomes a " + chosenSubtype.getDisplayName() + " until end of turn.")
                .build());
    }
}
