package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordsToCastSpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantKeywordsToCastSpellEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantKeywordsToCastSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GrantKeywordsToCastSpellEffect grant = (GrantKeywordsToCastSpellEffect) effect;
        if (entry.getTriggeringCardId() == null) {
            return;
        }

        for (StackEntry spellEntry : gameData.stack) {
            if (spellEntry.getCard() == null
                    || !entry.getTriggeringCardId().equals(spellEntry.getCard().getId())) {
                continue;
            }

            spellEntry.getGrantedKeywordsOnEntry().addAll(grant.keywords());
            gameLogService.append(gameData, GameLog.cardThen(spellEntry.getCard(),
                    " gains " + grant.keywords() + " until end of turn."));
            log.info("Game {} - {} gains {} until end of turn",
                    gameData.id, spellEntry.getCard().getName(), grant.keywords());
            return;
        }
    }
}
