package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaReanimateCreaturesWithTotalManaValueXEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayXManaReanimateCreaturesWithTotalManaValueXEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final TriggeredAbilityStackService triggeredAbilityStackService;
    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;
    private final PredicateEvaluationService predicateEvaluationService;

    /**
     * "Any number of target creature cards with total mana value X or less": X is the X paid, which the reflexive
     * ability's entry has as its X.
     */
    private static final ReturnTargetCardsFromGraveyardToBattlefieldEffect RETURN_WITHIN_TOTAL_MANA_VALUE_X =
            new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                    new CardTypePredicate(CardType.CREATURE), 0, false, false,
                    null, 0, new XValue(), null, null, null, 0,
                    GraveyardSearchScope.ALL_GRAVEYARDS, false, false, false, 0, 0);

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayXManaReanimateCreaturesWithTotalManaValueXEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        Card sourceCard = entry.getCard();
        String cardName = sourceCard.getName();
        String playerName = gameData.playerIdToName.get(controllerId);

        if (gameData.chosenXValue != null) {
            int chosenValue = gameData.chosenXValue;
            gameData.chosenXValue = null;

            if (chosenValue == 0) {
                // Paying {0} is still paying, so the reflexive trigger happens (for total mana value 0 or less).
                gameLogService.append(gameData,
                        GameLog.text(playerName + " pays {0} for " + cardName + "."));
                beginReflexiveTrigger(gameData, entry, controllerId, 0);
                return;
            }

            ManaPool pool = gameData.playerManaPools.get(controllerId);
            if (payableFromPool(pool) < chosenValue) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " can't pay {" + chosenValue + "} for " + cardName
                                + " (tap mana sources, then choose X again)."));
                beginXPrompt(gameData, controllerId, cardName);
                return;
            }

            new ManaCost("{X}").pay(pool, chosenValue);
            gameLogService.append(gameData,
                    GameLog.text(playerName + " pays {" + chosenValue + "} for " + cardName + "."));
            beginReflexiveTrigger(gameData, entry, controllerId, chosenValue);
            return;
        }

        int maxX = maxPotentialX(gameData, controllerId);
        if (reanimationCandidates(gameData, entry, controllerId, maxX).isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.textCardText(playerName + " can't return any creature card with ", sourceCard,
                            "'s ability."));
            return;
        }
        beginXPrompt(gameData, controllerId, cardName);
    }

    private void beginReflexiveTrigger(GameData gameData, StackEntry entry,
                                       UUID controllerId, int x) {
        Card sourceCard = entry.getCard();
        ReturnTargetCardsFromGraveyardToBattlefieldEffect returnEffect = RETURN_WITHIN_TOTAL_MANA_VALUE_X;

        // "When you do" is an ability of its own (CR 603.12): its graveyard targets are chosen as it goes on the stack,
        // against the X paid. It records what the resolving entry was about.
        StackEntry record = ReflexiveAbilitySupport.record(gameData, entry, List.of(returnEffect),
                sourceCard.getName() + "'s ability");
        record.setXValue(x);
        triggeredAbilityStackService.putMade(gameData, record);
    }

    /** Creature cards in the damaged player's graveyard with mana value at most {@code x} that can be targeted. */
    private List<Card> reanimationCandidates(GameData gameData, StackEntry entry, UUID controllerId, int x) {
        List<Card> candidates = new ArrayList<>();
        UUID damagedPlayerId = entry.getTargetId();
        if (damagedPlayerId != null && gameQueryService.canGraveyardCardsBeTargeted(gameData)) {
            CardPredicate creatureFilter = new CardTypePredicate(CardType.CREATURE);
            for (Card card : gameData.playerGraveyards.getOrDefault(damagedPlayerId, List.of())) {
                if (card.getManaValue() <= x
                        && !gameQueryService.isLandCardTargetRestricted(gameData, card, controllerId)
                        && predicateEvaluationService.matchesCardPredicate(
                        card, creatureFilter, entry.getCard().getId())) {
                    candidates.add(card);
                }
            }
        }
        return candidates;
    }

    private void beginXPrompt(GameData gameData, UUID controllerId, String cardName) {
        int maxX = maxPotentialX(gameData, controllerId);
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(controllerId, maxX,
                        "Pay {X} for " + cardName + "? Return creature cards from that player's graveyard.",
                        cardName, true));
    }

    private int maxPotentialX(GameData gameData, UUID controllerId) {
        int untappedSources = potentialManaService.buildVirtualManaPool(gameData, controllerId).getTotal()
                - gameData.playerManaPools.get(controllerId).getTotal();
        return payableFromPool(gameData.playerManaPools.get(controllerId)) + untappedSources;
    }

    private static int payableFromPool(ManaPool pool) {
        return pool.getTotal() + pool.getArtifactOnlyColorless()
                + pool.getMyrOnlyColorless() + pool.getXCostOnlyColorless();
    }
}
