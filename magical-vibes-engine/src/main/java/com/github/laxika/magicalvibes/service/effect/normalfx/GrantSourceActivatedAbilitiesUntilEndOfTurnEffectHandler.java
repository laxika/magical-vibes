package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSourceActivatedAbilitiesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantSourceActivatedAbilitiesUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantSourceActivatedAbilitiesUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantSourceActivatedAbilitiesUntilEndOfTurnEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getDescription() + " fizzles (source no longer on battlefield)."));
            return;
        }

        source.getTemporaryActivatedAbilities().addAll(e.abilities());
        String logEntry = source.getCard().getName() + " gains activated abilities of "
                + e.copiedFromCardName() + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(source.getCard()).text(" gains activated abilities of " + e.copiedFromCardName() + " until end of turn.").build());
        log.info("Game {} - {} gains {} activated ability/abilities from {}", gameData.id,
                source.getCard().getName(), e.abilities().size(), e.copiedFromCardName());
    }
}
