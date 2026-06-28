package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterlashEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
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
public class CounterlashEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterlashEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }

        if (targetEntry == null) {
            log.info("Game {} - Counterlash target no longer on stack", gameData.id);
            return;
        }

        Set<CardType> matchingTypes = new HashSet<>();
        matchingTypes.add(targetEntry.getCard().getType());
        matchingTypes.addAll(targetEntry.getCard().getAdditionalTypes());

        if (gameQueryService.isUncounterable(gameData, targetEntry.getCard())) {
            log.info("Game {} - {} cannot be countered", gameData.id, targetEntry.getCard().getName());
        } else if (gameQueryService.isProtectedFromCounterBySpellColor(gameData, targetEntry.getControllerId(), entry)) {
            log.info("Game {} - {} cannot be countered by {} spells",
                    gameData.id, targetEntry.getCard().getName(),
                    entry.getCard().getColor().name().toLowerCase());
        } else {
            counterSupport.counterSpell(gameData, entry, targetEntry);
        }

        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) return;

        List<Card> eligible = hand.stream()
                .filter(c -> !c.hasType(CardType.LAND) && counterSupport.sharesCardType(c, matchingTypes))
                .toList();

        if (!eligible.isEmpty()) {
            for (int i = eligible.size() - 1; i >= 0; i--) {
                Card c = eligible.get(i);
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        c, controllerId,
                        List.of(new MayCastFromHandWithoutPayingManaCostEffect()),
                        "Cast " + c.getName() + " without paying its mana cost?"
                ));
            }
        }
    }
}
