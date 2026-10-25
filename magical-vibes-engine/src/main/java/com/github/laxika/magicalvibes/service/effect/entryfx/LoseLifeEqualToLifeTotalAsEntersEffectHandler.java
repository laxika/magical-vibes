package com.github.laxika.magicalvibes.service.effect.entryfx;

import com.github.laxika.magicalvibes.model.BattlefieldEntryRequest;
import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEqualToLifeTotalAsEntersEffect;
import com.github.laxika.magicalvibes.service.StackResolutionService;
import com.github.laxika.magicalvibes.service.battlefield.AsEntersInteractionService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldPlacementService;
import com.github.laxika.magicalvibes.service.battlefield.DepartureService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EntryReplacementHandlerBean;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Lich's "As this enchantment enters, you lose life equal to your life total." The loss is part of the permanent's
 * entering, so the Lich's Duel Mastery sacrifice that replaces it has its death replacements chosen before the
 * permanent enters (CR 616.1f): a resolving permanent spell with this replacement asks them before its resolution
 * begins ({@link #askBeforeResolving}), and a permanent an effect puts onto the battlefield asks them before its entry
 * changes anything ({@link #askBeforeEntering}). Either goes on once they are answered
 * ({@link #resolveAfterDepartureQuestions}), the loss's sacrifice performing the answered batch.
 */
@Component
@RequiredArgsConstructor
public class LoseLifeEqualToLifeTotalAsEntersEffectHandler implements EntryReplacementHandlerBean {

    private static final Set<StackEntryType> PERMANENT_SPELLS = EnumSet.of(StackEntryType.CREATURE_SPELL,
            StackEntryType.ENCHANTMENT_SPELL, StackEntryType.ARTIFACT_SPELL, StackEntryType.PLANESWALKER_SPELL,
            StackEntryType.BATTLE_SPELL);

    private final LifeSupport lifeSupport;
    private final GameQueryService gameQueryService;
    private final ObjectProvider<DepartureService> departureService;
    private final ObjectProvider<StackResolutionService> stackResolutionService;
    private final ObjectProvider<BattlefieldPlacementService> battlefieldPlacementService;
    private final ObjectProvider<BattlefieldEntryBatchSupport> battlefieldEntryBatchSupport;
    private final ObjectProvider<AsEntersInteractionService> asEntersInteractionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseLifeEqualToLifeTotalAsEntersEffect.class;
    }

    @Override
    public void apply(GameData gameData, UUID controllerId, Permanent enteringPermanent, CardEffect effect) {
        DepartureIntent sacrifice = lifeLossSacrifice(gameData, controllerId);
        lifeSupport.applyLifeLoss(gameData, controllerId, gameData.getLife(controllerId),
                enteringPermanent.getCard().getName());
        if (sacrifice != null) {
            DepartureService.discardAnsweredEventBatch(gameData, sacrifice);
        }
    }

    /**
     * Asks, before {@code entry} (the top of the stack) begins to resolve, the questions of the Lich's Duel Mastery
     * sacrifice that would replace the life loss of its "as this enters" replacement. Nothing has changed when this
     * returns; when a question is opened the resolution must stop, with the spell still on the stack.
     *
     * @return {@code true} when a question is open
     */
    public boolean askBeforeResolving(GameData gameData, StackEntry entry) {
        if (!PERMANENT_SPELLS.contains(entry.getEntryType()) || entry.getCard() == null
                || entry.getCard().getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .noneMatch(LoseLifeEqualToLifeTotalAsEntersEffect.class::isInstance)) {
            return false;
        }
        DepartureIntent sacrifice = lifeLossSacrifice(gameData, entry.getControllerId());
        if (sacrifice == null
                || !departureService.getObject().askBeforeEvent(gameData, DepartureBatch.of(List.of(sacrifice)))) {
            return false;
        }
        gameData.spellAwaitingEntryLifeLossDepartureId = entry.getTargetableId();
        return true;
    }

    /**
     * Asks, before {@code request}'s permanent is put onto the battlefield (by an effect: a reanimation, a "put it
     * onto the battlefield"), the questions of the Lich's Duel Mastery sacrifice that would replace the life loss of
     * its "as this enters" replacement. Nothing of the entry has happened when this returns; when a question is
     * opened the entry must stop, and it goes on once the questions are answered.
     *
     * @return {@code true} when a question is open
     */
    public boolean askBeforeEntering(GameData gameData, BattlefieldEntryRequest request) {
        Permanent entering = request.permanent();
        if (entering.isFaceDown() || entering.getCard().getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .noneMatch(LoseLifeEqualToLifeTotalAsEntersEffect.class::isInstance)
                || gameQueryService.hasLostPrintedAbilitiesAsEntering(gameData, request.controllerId(), entering)) {
            return false;
        }
        DepartureIntent sacrifice = lifeLossSacrifice(gameData, request.controllerId());
        if (sacrifice == null
                || !departureService.getObject().askBeforeEvent(gameData, DepartureBatch.of(List.of(sacrifice)))) {
            return false;
        }
        gameData.entryAwaitingLifeLossDeparture = request;
        return true;
    }

    /**
     * Resolves the spell {@link #askBeforeResolving} stopped, or goes on with the entry {@link #askBeforeEntering}
     * stopped, once every question is answered.
     *
     * @return {@code true} while a question is still open
     */
    public boolean resolveAfterDepartureQuestions(GameData gameData) {
        BattlefieldEntryRequest request = gameData.entryAwaitingLifeLossDeparture;
        UUID entryId = gameData.spellAwaitingEntryLifeLossDepartureId;
        if (entryId == null && request == null) {
            return false;
        }
        if (departureService.getObject().continueAskingBeforeEvent(gameData)) {
            return true;
        }
        if (request != null) {
            gameData.entryAwaitingLifeLossDeparture = null;
            continueEntry(gameData, request);
            return false;
        }
        gameData.spellAwaitingEntryLifeLossDepartureId = null;
        StackEntry top = gameData.stack.isEmpty() ? null : gameQueryService.stackUsesFirstInFirstOut(gameData)
                ? gameData.stack.getFirst() : gameData.stack.getLast();
        if (top != null && entryId.equals(top.getTargetableId())) {
            stackResolutionService.getObject().resolveTopOfStack(gameData);
        }
        return false;
    }

    /** Goes on with an entry stopped before it began, as the other "as enters" choices made before entering do. */
    private void continueEntry(GameData gameData, BattlefieldEntryRequest request) {
        if (battlefieldEntryBatchSupport.getObject().completeNativeChoice(gameData, request)) {
            return;
        }
        battlefieldPlacementService.getObject().place(gameData, request);
        Permanent entering = request.permanent();
        if (gameQueryService.findPermanentById(gameData, entering.getId()) == null) {
            return;
        }
        StackEntry spell = request.sourceStackEntry();
        asEntersInteractionService.getObject().handleCreatureEnteredBattlefield(gameData, request.controllerId(),
                entering.getCard(), spell == null ? null : spell.getTargetId(),
                entering.getCastFromZone() == Zone.HAND,
                spell != null && spell.getEtbMode() != null ? spell.getEtbMode() : request.xValue(),
                request.xValue(), request.kicked(), spell == null ? List.of() : spell.getTargetIds(),
                request.repeatedAdditionalCosts(),
                spell == null ? List.of() : spell.getConvokeCreatureIds());
    }

    /** The sacrifice that would replace {@code playerId} losing life equal to their life total now, or null. */
    private DepartureIntent lifeLossSacrifice(GameData gameData, UUID playerId) {
        if (gameData.getLife(playerId) <= 0 || !gameQueryService.canPlayerLoseLife(gameData, playerId)) {
            return null;
        }
        return lifeSupport.lifeLossSacrifice(gameData, playerId);
    }
}
