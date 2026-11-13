package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.model.filter.CardManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaCastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayXManaCastTargetInstantOrSorceryFromGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final TriggeredAbilityStackService triggeredAbilityStackService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;

    /** "Instant or sorcery card with mana value X": X is the X paid, which the reflexive ability's entry has. */
    private static final CardPredicate INSTANT_OR_SORCERY_WITH_MANA_VALUE_X = new CardAllOfPredicate(List.of(
            new CardAnyOfPredicate(List.of(
                    new CardTypePredicate(CardType.INSTANT),
                    new CardTypePredicate(CardType.SORCERY))),
            new CardManaValueEqualsXPredicate()));

    private static final CastTargetInstantOrSorceryFromGraveyardEffect CAST_WITH_MANA_VALUE_X =
            new CastTargetInstantOrSorceryFromGraveyardEffect(
                    GraveyardSearchScope.ALL_GRAVEYARDS, true, true, INSTANT_OR_SORCERY_WITH_MANA_VALUE_X);

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayXManaCastTargetInstantOrSorceryFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        Card sourceCard = entry.getCard();
        String cardName = sourceCard.getName();
        String playerName = gameData.playerIdToName.get(controllerId);

        if (gameData.chosenXValue == null) {
            beginXPrompt(gameData, controllerId, cardName);
            return;
        }

        int chosenX = gameData.chosenXValue;
        gameData.chosenXValue = null;

        ManaPool pool = gameData.playerManaPools.get(controllerId);
        if (payableFromPool(pool) < chosenX) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " can't pay {" + chosenX + "} for " + cardName
                            + " (tap mana sources, then choose X again)."));
            beginXPrompt(gameData, controllerId, cardName);
            return;
        }

        new ManaCost("{X}").pay(pool, chosenX);
        gameLogService.append(gameData, GameLog.text(
                playerName + " pays {" + chosenX + "} for " + cardName + "."));
        beginReflexiveTrigger(gameData, entry, chosenX);
    }

    private void beginReflexiveTrigger(GameData gameData, StackEntry entry, int chosenX) {
        Card sourceCard = entry.getCard();
        // "When you do, you may cast target instant or sorcery card with mana value X from a graveyard" (CR
        // 603.12): its graveyard target is chosen as it goes on the stack, against the X paid, the ability's X.
        StackEntry record = ReflexiveAbilitySupport.record(gameData, entry, List.of(CAST_WITH_MANA_VALUE_X),
                sourceCard.getName() + "'s ability");
        record.setXValue(chosenX);
        triggeredAbilityStackService.putMade(gameData, record);
    }

    private void beginXPrompt(GameData gameData, UUID controllerId, String cardName) {
        int maxX = maxPotentialX(gameData, controllerId);
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(
                        controllerId,
                        maxX,
                        "Pay {X} for " + cardName
                                + "? Cast an instant or sorcery card with mana value X from a graveyard.",
                        cardName,
                        true));
    }

    private int maxPotentialX(GameData gameData, UUID controllerId) {
        int untappedSources = potentialManaService.buildVirtualManaPool(gameData, controllerId).getTotal()
                - gameData.playerManaPools.get(controllerId).getTotal();
        return Math.max(0, payableFromPool(gameData.playerManaPools.get(controllerId)) + untappedSources);
    }

    private static int payableFromPool(ManaPool pool) {
        return pool.getTotal() + pool.getArtifactOnlyColorless()
                + pool.getMyrOnlyColorless() + pool.getXCostOnlyColorless();
    }

}
