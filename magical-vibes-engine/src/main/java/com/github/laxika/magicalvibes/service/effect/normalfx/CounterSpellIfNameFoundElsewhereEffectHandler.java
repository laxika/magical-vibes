package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellIfNameFoundElsewhereEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Bazaar of Wonders: counters the triggering spell only if a card with the same name sits in any
 * graveyard, or a nontoken permanent with the same name is on any battlefield. The spell itself is
 * on the stack while this resolves, so it never matches itself.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CounterSpellIfNameFoundElsewhereEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterSpellIfNameFoundElsewhereEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetCardId, entry);
        if (targetEntry == null) return;

        String spellName = targetEntry.getCard().getName();
        if (!nameFoundElsewhere(gameData, spellName)) {
            gameLogService.append(gameData, GameLog.text(
                    entry.getCard().getName() + " does not counter " + spellName
                            + " — no card with that name in a graveyard or on the battlefield."));
            return;
        }

        counterSupport.counterSpell(gameData, entry, targetEntry);
    }

    private boolean nameFoundElsewhere(GameData gameData, String spellName) {
        for (List<Card> graveyard : gameData.playerGraveyards.values()) {
            for (Card card : graveyard) {
                if (spellName.equals(card.getName())) return true;
            }
        }
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                Card card = permanent.getCard();
                if (!card.isToken() && spellName.equals(card.getName())) return true;
            }
        }
        return false;
    }
}
