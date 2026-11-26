package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.BattlefieldEntryCard;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Puts the named cards onto the battlefield as one batch, which asks its entry choices at the end of the step. How many
 * counters they enter with is worked out once, before any of them moves (CR 608.2h).
 */
@Component
@RequiredArgsConstructor
public class PutCardsOntoBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCardsOntoBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var put = (PutCardsOntoBattlefieldEffect) effect;
        List<BattlefieldEntryCard> entering = new ArrayList<>();
        entry.recordStep(StepKind.RETURN, StepOutcome.NONE);
        UUID named = put.controller() == null ? null
                : subjectResolutionService.player(gameData, entry, put, put.controller());
        if (put.controller() != null && named == null) {
            return;
        }
        var enteringAs = put.entering() == null ? null : put.entering().withCounterCount(
                put.entering().counter() == null ? 0 : Math.max(0, amountEvaluationService.evaluate(
                        gameData, put.entering().counters(), AmountContext.forStackEntry(entry, null))));
        var host = enteringAs == null ? null : enteringAs.host();
        List<UUID> hosts = host == null ? null : host.permanents() != null
                ? subjectResolutionService.permanents(gameData, entry, put, host.permanents()).stream()
                .map(Permanent::getId).toList()
                : List.copyOf(subjectResolutionService.players(gameData, entry, put, host.players()));
        for (SubjectResolutionService.LocatedCard located
                : subjectResolutionService.locatedCards(gameData, entry, put, put.cards())) {
            if (located.zone() == Zone.BATTLEFIELD) {
                // "This card" named while it is still a permanent (sourceObjectCard()) is already there.
                continue;
            }
            if (located.card().isToken()) {
                // A token that has left the battlefield can't come back onto it (CR 111.8).
                continue;
            }
            Permanent permanent = new Permanent(located.card(), located.zone());
            var faceDown = enteringAs == null ? null : enteringAs.faceDown();
            if (faceDown != null) {
                permanent.setFaceDown(faceDown.power(), faceDown.toughness(), faceDown.types(), faceDown.subtypes());
                permanent.setManifested(faceDown.manifested());
            }
            UUID controllerId = battlefieldEntryService.resolveEnteringController(
                    gameData, named == null ? located.ownerId() : named, permanent);
            BattlefieldEntryCard entryCard = new BattlefieldEntryCard(
                    controllerId, located.ownerId(), located.card(), located.zone(), null).withTapped(put.tapped());
            if (enteringAs != null || put.controller() != null) {
                // Also marks that the instruction names who controls it, who then keeps another player's card.
                entryCard = entryCard.withEntering(enteringAs != null ? enteringAs
                        : new PutCardsOntoBattlefieldEffect.EnteringAs(null, null, new Fixed(0), List.of(), null));
            }
            if (hosts != null) {
                entryCard = entryCard.withHosts(hosts);
            }
            if (faceDown != null) {
                // Face down it has no copy or other entry ability to ask about (CR 708.3).
                entryCard = entryCard.withPreparedPermanent(permanent, List.of());
            }
            entering.add(entryCard.recordingReturn());
        }
        if (!entering.isEmpty()) {
            battlefieldEntryBatchSupport.begin(gameData, entering);
        }
    }
}
