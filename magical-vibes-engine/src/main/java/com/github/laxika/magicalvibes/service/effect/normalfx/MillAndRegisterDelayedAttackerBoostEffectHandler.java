package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedAttackerBoost;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillAndRegisterDelayedAttackerBoostEffect;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MillAndRegisterDelayedAttackerBoostEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillAndRegisterDelayedAttackerBoostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MillAndRegisterDelayedAttackerBoostEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        int cardsToMill = Math.min(Math.max(0, e.millCount()), deck.size());

        // Snapshot before mill so "put into your graveyard this way" can be counted after
        // replacements that redirect milled cards elsewhere.
        List<Card> preview = cardsToMill == 0
                ? List.of()
                : new ArrayList<>(deck.subList(0, cardsToMill));

        if (cardsToMill > 0) {
            graveyardService.resolveMillPlayer(gameData, controllerId, e.millCount());
        }

        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        Set<Card> inGraveyard = graveyard == null ? Set.of() : new HashSet<>(graveyard);
        int creatureCount = 0;
        for (Card card : preview) {
            if (card.hasType(CardType.CREATURE) && inGraveyard.contains(card)) {
                creatureCount++;
            }
        }

        int power = creatureCount * e.powerPerCreature();
        int toughness = creatureCount * e.toughnessPerCreature();
        if (power == 0 && toughness == 0) {
            log.info("Game {} - {} milled {} creature card(s); no delayed attacker boost registered",
                    gameData.id, entry.getCard().getName(), creatureCount);
            return;
        }

        gameData.queueDelayedAction(
                new DelayedAttackerBoost(controllerId, power, toughness, entry.getCard()));
        log.info("Game {} - {} registers delayed attacker boost +{}/+{} ({} creature card(s) milled)",
                gameData.id, entry.getCard().getName(), power, toughness, creatureCount);
    }
}
