package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardAndBoostSelfIfCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardAndBoostSelfIfCreatureEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final BoostSelfEffectHandler boostSelfEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardAndBoostSelfIfCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String controllerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.text(controllerName + "'s library is empty — nothing to exile."));
            return;
        }

        Card topCard = deck.removeFirst();
        exileService.exileCard(gameData, controllerId, topCard);
        gameLogService.append(gameData, GameLog.textCardText(controllerName + " exiles ", topCard,
                " from the top of their library."));

        if (!topCard.hasType(CardType.CREATURE)) {
            return;
        }

        int power = topCard.getPower() != null ? topCard.getPower() : 0;
        int toughness = topCard.getToughness() != null ? topCard.getToughness() : 0;
        boostSelfEffectHandler.resolve(gameData, entry, new BoostSelfEffect(power, toughness));
        log.info("Game {} - {} gets {}/{} from exiled creature {}", gameData.id,
                entry.getCard().getName(), power, toughness, topCard.getName());
    }
}
