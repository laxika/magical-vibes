package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardAndCounterTriggeringSpellIfManaValueMatchesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevealTopCardAndCounterTriggeringSpellIfManaValueMatchesEffectHandler
        implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardAndCounterTriggeringSpellIfManaValueMatchesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> library = gameData.playerDecks.get(entry.getControllerId());
        if (library == null || library.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(entry.getControllerId()) + "'s library is empty."));
            return;
        }

        Card topCard = library.getFirst();
        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(entry.getControllerId()) + " reveals ")
                .card(topCard)
                .text(" from the top of their library.")
                .build());

        UUID triggeringCardId = entry.getTriggeringCardId();
        if (triggeringCardId == null) {
            return;
        }

        StackEntry triggeringSpell = counterSupport.findCounterTarget(gameData, triggeringCardId, entry);
        if (triggeringSpell == null) {
            return;
        }

        int spellManaValue = triggeringSpell.getCard().getManaValue() + triggeringSpell.getXValue();
        if (topCard.getManaValue() == spellManaValue) {
            counterSupport.counterSpell(gameData, entry, triggeringSpell);
        }
    }
}
