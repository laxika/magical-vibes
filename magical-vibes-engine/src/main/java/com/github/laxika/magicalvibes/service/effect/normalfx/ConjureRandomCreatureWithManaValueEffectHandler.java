package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCreatureWithManaValueEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class ConjureRandomCreatureWithManaValueEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final Map<Integer, List<CardPrinting>> candidatesByManaValue = new ConcurrentHashMap<>();

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureRandomCreatureWithManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureRandomCreatureWithManaValueEffect typed =
                (ConjureRandomCreatureWithManaValueEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }

        int manaValue = amountEvaluationService.evaluate(
                gameData,
                typed.manaValue(),
                AmountContext.forStackEntry(entry, source));
        List<CardPrinting> candidates = candidatesByManaValue.computeIfAbsent(
                manaValue, this::findCandidates);
        if (candidates.isEmpty()) {
            return;
        }

        Card card = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size())).createCard();
        card.setOwnerId(entry.getControllerId());
        Permanent permanent = new Permanent(card);
        permanent.getGrantedKeywords().add(Keyword.HASTE);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), permanent);
        battlefieldEntryService.handleCreatureEnteredBattlefield(
                gameData, entry.getControllerId(), card, null, false);
        entry.getCreatedPermanentIds().add(permanent.getId());
        gameData.queueDelayedAction(new DelayedPermanentAction(
                permanent.getId(), DelayedPermanentActionKind.EXILE_AT_END_STEP));

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " conjures ")
                .card(card)
                .text(" onto the battlefield with haste.")
                .build());
    }

    private List<CardPrinting> findCandidates(int manaValue) {
        Map<String, CardPrinting> uniquePrintings = new LinkedHashMap<>();
        for (CardSet set : CardSet.values()) {
            for (CardPrinting printing : cardCatalog.getPrintings(set)) {
                uniquePrintings.putIfAbsent(printing.cardClassName(), printing);
            }
        }
        return uniquePrintings.values().stream()
                .filter(printing -> {
                    Card card = printing.createCard();
                    return card.hasType(CardType.CREATURE) && card.getManaValue() == manaValue;
                })
                .toList();
    }
}
