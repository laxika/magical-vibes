package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.ResolutionResults;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CardsStillInZone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * The delayed triggered abilities about exiled cards that the engine makes where no exile step of a resolution does
 * ({@code RegisterDelayedStepTriggerEffect} makes those): each carries the cards bound to their stay as that step's
 * outcome, so {@code Subjects.cardsThisWay(StepKind.EXILE)} in its effects names one only while it is still that
 * object (CR 400.7, 603.7c).
 */
public final class DelayedExiledCardReturns {

    private DelayedExiledCardReturns() {
    }

    /** Referents naming {@code cardIds}, each in the exile stay it is in now; {@code null} ids are none. */
    public static DelayedReferents exiled(GameData gameData, List<UUID> cardIds) {
        List<UUID> ids = cardIds.stream().filter(Objects::nonNull).toList();
        List<MovedCard> stays = ids.stream()
                .map(id -> gameData.boundToStay(new MovedCard(id, id, Zone.EXILE, null)))
                .toList();
        return DelayedReferents.cards(ids).withStepResults(ResolutionResults.EMPTY.withOutcome(
                StepKind.EXILE, StepOutcome.ofObjects(ids, List.of(), stays)));
    }

    /**
     * "At the beginning of the next end step, return those cards to their owner's hand", then draw {@code thenDraw}
     * cards, made by {@code controllerId}; with {@code onlyIfStillExiled} it triggers only if one still is ("if you
     * haven't cast it", CR 603.4).
     */
    public static TemporaryGlobalTriggeredAbility toHandAtNextEndStep(GameData gameData, UUID controllerId,
                                                                      Card sourceCard, UUID sourcePermanentId,
                                                                      List<UUID> cardIds, int thenDraw,
                                                                      boolean onlyIfStillExiled) {
        return toHandAtEndStep(ActivePlayer.ANY, gameData, controllerId, sourceCard, sourcePermanentId, cardIds,
                thenDraw, onlyIfStillExiled);
    }

    /** "At the beginning of your next end step, put that card into your hand" (Necropotence). */
    public static TemporaryGlobalTriggeredAbility toHandAtYourNextEndStep(GameData gameData, UUID controllerId,
                                                                          Card sourceCard, UUID sourcePermanentId,
                                                                          UUID cardId) {
        return toHandAtEndStep(ActivePlayer.YOU, gameData, controllerId, sourceCard, sourcePermanentId,
                List.of(cardId), 0, false);
    }

    private static TemporaryGlobalTriggeredAbility toHandAtEndStep(ActivePlayer endStepOf, GameData gameData,
                                                                   UUID controllerId, Card sourceCard,
                                                                   UUID sourcePermanentId, List<UUID> cardIds,
                                                                   int thenDraw, boolean onlyIfStillExiled) {
        List<CardEffect> effects = new ArrayList<>();
        effects.add(new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.EXILE)));
        if (thenDraw > 0) {
            effects.add(new DrawCardEffect(thenDraw));
        }
        TriggerOptions options = onlyIfStillExiled
                ? TriggerOptions.DEFAULT.withInterveningIf(
                        new CardsStillInZone(Zone.EXILE, gameData.exileEntryVersionsOf(cardIds)))
                : TriggerOptions.DEFAULT;
        return TemporaryGlobalTriggeredAbility.atNextStep(controllerId, sourceCard,
                new TriggeredAbility(Triggers.endStep(endStepOf), options, effects.toArray(CardEffect[]::new)),
                sourcePermanentId, null, null, exiled(gameData, cardIds));
    }
}
