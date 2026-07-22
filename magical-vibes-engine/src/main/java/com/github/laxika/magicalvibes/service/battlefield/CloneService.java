package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CloneService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final LegendRuleService legendRuleService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentCopierService permanentCopierService;
    private final AmountEvaluationService amountEvaluationService;

    public boolean prepareCloneReplacementEffect(GameData gameData, UUID controllerId, Card card, UUID targetId) {
        return prepareCloneReplacementEffect(gameData, controllerId, card, targetId, 0);
    }

    public boolean prepareCloneReplacementEffect(GameData gameData, UUID controllerId, Card card, UUID targetId,
                                                 int xValue) {
        CopyPermanentOnEnterEffect copyEffect = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(e -> e instanceof CopyPermanentOnEnterEffect)
                .map(e -> (CopyPermanentOnEnterEffect) e)
                .findFirst().orElse(null);
        if (copyEffect == null) return false;

        List<UUID> validIds = new ArrayList<>();
        gameData.forEachPermanent((pid, p) -> {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, p, copyEffect.filter())) {
                validIds.add(p.getId());
            }
        });

        if (validIds.isEmpty()) return false;

        gameData.cloneOperation.card = card;
        gameData.cloneOperation.controllerId = controllerId;
        gameData.cloneOperation.etbTargetId = targetId;
        gameData.cloneOperation.powerOverride = copyEffect.powerOverride();
        gameData.cloneOperation.toughnessOverride = copyEffect.toughnessOverride();
        gameData.cloneOperation.additionalTypesOverride = copyEffect.additionalTypesOverride();
        gameData.cloneOperation.additionalActivatedAbilities = copyEffect.additionalActivatedAbilities();
        gameData.cloneOperation.embalmColorOverride = copyEffect.embalmColorOverride();
        gameData.cloneOperation.embalmAddedSubtype = copyEffect.embalmAddedSubtype();
        gameData.cloneOperation.embalmRemoveManaCost = copyEffect.embalmRemoveManaCost();
        gameData.cloneOperation.additionalPlusOnePlusOneCounters = copyEffect.additionalPlusOnePlusOneCounters();
        gameData.cloneOperation.xValue = xValue;
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.CloneCopy());

        gameData.pendingMayAbilities.add(new PendingMayAbility(
                card,
                controllerId,
                List.of(copyEffect),
                card.getName() + " — You may have it enter as a copy of any " + copyEffect.typeLabel() + " on the battlefield."
        ));
        playerInputService.processNextMayAbility(gameData);
        return true;
    }

    public void completeCloneEntry(GameData gameData, UUID targetId) {
        Card card = gameData.cloneOperation.card;
        UUID controllerId = gameData.cloneOperation.controllerId;
        UUID etbTargetId = gameData.cloneOperation.etbTargetId;
        Integer powerOverride = gameData.cloneOperation.powerOverride;
        Integer toughnessOverride = gameData.cloneOperation.toughnessOverride;
        Set<CardType> additionalTypesOverride = gameData.cloneOperation.additionalTypesOverride;
        List<ActivatedAbility> additionalActivatedAbilities = gameData.cloneOperation.additionalActivatedAbilities;
        CardColor embalmColorOverride = gameData.cloneOperation.embalmColorOverride;
        CardSubtype embalmAddedSubtype = gameData.cloneOperation.embalmAddedSubtype;
        boolean embalmRemoveManaCost = gameData.cloneOperation.embalmRemoveManaCost;
        DynamicAmount additionalPlusOnePlusOneCounters = gameData.cloneOperation.additionalPlusOnePlusOneCounters;
        int xValue = gameData.cloneOperation.xValue;

        gameData.cloneOperation.card = null;
        gameData.cloneOperation.controllerId = null;
        gameData.cloneOperation.etbTargetId = null;
        gameData.cloneOperation.powerOverride = null;
        gameData.cloneOperation.toughnessOverride = null;
        gameData.cloneOperation.additionalTypesOverride = Set.of();
        gameData.cloneOperation.additionalActivatedAbilities = List.of();
        gameData.cloneOperation.embalmColorOverride = null;
        gameData.cloneOperation.embalmAddedSubtype = null;
        gameData.cloneOperation.embalmRemoveManaCost = false;
        gameData.cloneOperation.additionalPlusOnePlusOneCounters = null;
        gameData.cloneOperation.xValue = 0;

        Permanent perm = new Permanent(card);

        if (targetId != null) {
            Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetId);
            if (targetPerm != null) {
                permanentCopierService.applyCloneCopy(perm, targetPerm, powerOverride, toughnessOverride, additionalTypesOverride);
                // "except it has..." — add additional abilities to the copy (e.g. Evil Twin)
                for (ActivatedAbility extraAbility : additionalActivatedAbilities) {
                    perm.getCard().addActivatedAbility(extraAbility);
                }
                // Vizier of Many Faces embalm exception: a token that re-clones stays a token (so it
                // still ceases to exist on death), and the white / no-mana-cost / added-Zombie
                // transformation is re-applied on top of the freshly copied card — but only for an
                // embalm token; a hard-cast Clone keeps the copied creature's own color, cost, and types.
                if (card.isToken()) {
                    perm.getCard().setToken(true);
                    applyEmbalmExceptionToCopy(perm.getCard(), embalmColorOverride, embalmAddedSubtype, embalmRemoveManaCost);
                }
                // Altered Ego: "except it enters with X additional +1/+1 counters" — only when copying.
                // Applied before battlefield entry so ETB triggers / SBAs see the counters. Must be
                // done here (not via EnterWithCountersEffect) because the copy overwrites the card's
                // effects before putPermanentOntoBattlefield runs applyEnterWithCounters.
                applyAdditionalPlusOnePlusOneCounters(gameData, controllerId, perm,
                        additionalPlusOnePlusOneCounters, xValue);
            }
        }

        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm, xValue, false);

        String playerName = gameData.playerIdToName.get(controllerId);
        Card enteredCard = perm.getCard();
        if (targetId != null) {
            Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetId);
            if (targetPerm != null) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                        .card(enteredCard)
                        .text(" enters the battlefield as a copy of ")
                        .card(targetPerm.getCard())
                        .text(" under " + playerName + "'s control.")
                        .build());
                log.info("Game {} - {} enters as copy of {} for {}", gameData.id, enteredCard.getName(),
                        targetPerm.getCard().getName(), playerName);
            } else {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.entersBattlefieldUnder(enteredCard, playerName));
                log.info("Game {} - {} enters battlefield without copying for {}", gameData.id, enteredCard.getName(), playerName);
            }
        } else {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.entersBattlefieldUnder(enteredCard, playerName));
            log.info("Game {} - {} enters battlefield without copying for {}", gameData.id, enteredCard.getName(), playerName);
        }

        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, perm.getCard(), etbTargetId, true);

        if (!gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, controllerId);
        }
    }

    private void applyAdditionalPlusOnePlusOneCounters(GameData gameData, UUID controllerId, Permanent perm,
                                                       DynamicAmount amount, int xValue) {
        if (amount == null) return;
        if (gameQueryService.cantHaveCounters(gameData, perm)) return;
        int count = amountEvaluationService.evaluate(gameData, amount,
                new AmountContext(controllerId, perm, null, xValue, 0, false));
        if (count > 0) {
            perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                    perm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + count);
            log.info("Game {} - {} enters as copy with {} additional +1/+1 counter(s)",
                    gameData.id, perm.getCard().getName(), count);
        }
    }

    private void applyEmbalmExceptionToCopy(Card copy, CardColor embalmColorOverride,
                                            CardSubtype embalmAddedSubtype, boolean embalmRemoveManaCost) {
        if (embalmColorOverride != null) {
            copy.setColor(embalmColorOverride);
            copy.setColors(List.of(embalmColorOverride));
        }
        if (embalmRemoveManaCost) {
            copy.setManaCost("");
        }
        if (embalmAddedSubtype != null && !copy.getSubtypes().contains(embalmAddedSubtype)) {
            List<CardSubtype> subtypes = new ArrayList<>(copy.getSubtypes());
            subtypes.add(embalmAddedSubtype);
            copy.setSubtypes(subtypes);
        }
    }
}
