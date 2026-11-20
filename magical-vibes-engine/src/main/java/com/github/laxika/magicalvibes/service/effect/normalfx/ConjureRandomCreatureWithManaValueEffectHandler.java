package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
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
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.DelayedPermanentActions;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
        card.setConjured(true);
        card.setOwnerId(entry.getControllerId());
        Permanent permanent = new Permanent(card);
        if (typed.temporary()) {
            gameData.addEffectOnPermanent(entry, new GrantKeywordEffect(Keyword.HASTE, Subjects.target()),
                    permanent.getId(), Duration.UNTIL_END_OF_TURN);
        }
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, entry.getControllerId(), permanent);
        ActionEvents.conjured(gameData, entry.getControllerId(), 1);
        battlefieldEntryService.handleCreatureEnteredBattlefield(
                gameData, entry.getControllerId(), card, null, false);
        entry.getCreatedPermanentIds().add(permanent.getId());
        if (typed.temporary()) {
            DelayedPermanentActions.schedule(gameData, entry,
                    new DelayedPermanentAction(permanent.getId(), DelayedPermanentActionKind.EXILE_AT_END_STEP),
                    ActivePlayer.ANY);
        }

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " conjures ")
                .card(card)
                .text(typed.temporary()
                        ? " onto the battlefield with haste."
                        : " onto the battlefield.")
                .build());
    }

    private List<CardPrinting> findCandidates(int manaValue) {
        return cardCatalog.findPrintings(oracle ->
                oracle.hasType(CardType.CREATURE) && oracle.manaValue() == manaValue);
    }
}
