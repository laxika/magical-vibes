package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardAndCounterTriggeringSpellIfCardTypeMatchesEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPlayFreeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardAndCounterTriggeringSpellIfCardTypeMatchesEffectHandler
        implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardAndCounterTriggeringSpellIfCardTypeMatchesEffect.class;
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

        StackEntry triggeringSpell = counterSupport.findCounterTargetIgnoringCounterability(
                gameData, triggeringCardId, entry);
        if (triggeringSpell == null) {
            return;
        }

        Set<CardType> triggeringSpellTypes = EnumSet.of(triggeringSpell.getCard().getType());
        triggeringSpellTypes.addAll(triggeringSpell.getCard().getAdditionalTypes());
        if (!counterSupport.sharesCardType(topCard, triggeringSpellTypes)) {
            return;
        }

        StackEntry counterTarget = counterSupport.findCounterTarget(gameData, triggeringCardId, entry);
        if (counterTarget != null) {
            counterSupport.counterSpell(gameData, entry, counterTarget);
        }

        if (!topCard.hasType(CardType.LAND)) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    topCard,
                    triggeringSpell.getControllerId(),
                    List.of(new RevealTopCardMayPlayFreeEffect(
                            LookDestination.TOP_OF_LIBRARY, false, entry.getControllerId())),
                    "Cast " + topCard.getName() + " without paying its mana cost?"));
            log.info("Game {} - {} may cast {} without paying its mana cost",
                    gameData.id, gameData.playerIdToName.get(triggeringSpell.getControllerId()),
                    topCard.getName());
        }
    }
}
