package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
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
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CloneService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final LegendRuleService legendRuleService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentCopierService permanentCopierService;
    private final AmountEvaluationService amountEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;

    public boolean prepareCloneReplacementEffect(GameData gameData, UUID controllerId, Card card, UUID targetId) {
        return prepareCloneReplacementEffect(gameData, controllerId, card, targetId, 0);
    }

    public boolean prepareCloneReplacementEffect(GameData gameData, UUID controllerId, Card card, UUID targetId,
                                                 int xValue) {
        CopyPermanentOnEnterEffect copyEffect = findCopyEffect(gameData, controllerId, card);
        if (copyEffect == null) return false;

        FilterContext filterContext = FilterContext.of(gameData).withSourceControllerId(controllerId);
        List<UUID> validIds = new ArrayList<>();
        gameData.forEachPermanent((pid, p) -> {
            if (predicateEvaluationService.matchesPermanentPredicate(p, copyEffect.filter(), filterContext)) {
                validIds.add(p.getId());
            }
        });

        if (validIds.isEmpty()) return false;

        gameData.cloneOperation.card = card;
        gameData.cloneOperation.controllerId = controllerId;
        gameData.cloneOperation.etbTargetId = targetId;
        gameData.cloneOperation.powerOverride = copyEffect.powerOverride();
        gameData.cloneOperation.toughnessOverride = copyEffect.toughnessOverride();
        gameData.cloneOperation.copyPowerToughnessFromSource = copyEffect.copyPowerToughnessFromSource();
        gameData.cloneOperation.additionalTypesOverride = copyEffect.additionalTypesOverride();
        gameData.cloneOperation.additionalActivatedAbilities = copyEffect.additionalActivatedAbilities();
        gameData.cloneOperation.nameOverride = copyEffect.nameOverride();
        gameData.cloneOperation.additionalSupertypesOverride = copyEffect.additionalSupertypesOverride();
        gameData.cloneOperation.removedSupertypesOverride = copyEffect.removedSupertypesOverride();
        gameData.cloneOperation.addTypeAppropriateCounters = copyEffect.addTypeAppropriateCounters();
        gameData.cloneOperation.embalmColorOverride = copyEffect.embalmColorOverride();
        gameData.cloneOperation.embalmAddedSubtype = copyEffect.embalmAddedSubtype();
        gameData.cloneOperation.embalmRemoveManaCost = copyEffect.embalmRemoveManaCost();
        gameData.cloneOperation.additionalPlusOnePlusOneCounters = copyEffect.additionalPlusOnePlusOneCounters();
        gameData.cloneOperation.additionalSupertypesOverride = copyEffect.additionalSupertypesOverride();
        gameData.cloneOperation.additionalKeywordsOverride = copyEffect.additionalKeywordsOverride();
        gameData.cloneOperation.additionalCreatureOnlyCharacteristics = copyEffect.additionalCreatureOnlyCharacteristics();
        gameData.cloneOperation.additionalSubtypesOverride = copyEffect.additionalSubtypesOverride();
        gameData.cloneOperation.additionalSlotEffects = copyEffect.additionalSlotEffects();
        gameData.cloneOperation.copyColor = copyEffect.copyColor();
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

    private CopyPermanentOnEnterEffect findCopyEffect(GameData gameData, UUID controllerId, Card card) {
        ConditionContext conditionContext = ConditionContext.forCard(card, controllerId);
        for (CardEffect effect : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (effect instanceof CopyPermanentOnEnterEffect copyEffect) {
                return copyEffect;
            }
            if (effect instanceof ConditionalReplacementEffect conditional) {
                CardEffect selected = conditionEvaluationService.isMet(
                        gameData, conditional.condition(), conditionContext)
                        ? conditional.upgradedEffect() : conditional.baseEffect();
                if (selected instanceof CopyPermanentOnEnterEffect copyEffect) {
                    return copyEffect;
                }
            }
        }
        return null;
    }

    public void completeCloneEntry(GameData gameData, UUID targetId) {
        Card card = gameData.cloneOperation.card;
        UUID controllerId = gameData.cloneOperation.controllerId;
        UUID etbTargetId = gameData.cloneOperation.etbTargetId;
        Integer powerOverride = gameData.cloneOperation.powerOverride;
        Integer toughnessOverride = gameData.cloneOperation.toughnessOverride;
        boolean copyPowerToughnessFromSource = gameData.cloneOperation.copyPowerToughnessFromSource;
        Set<CardType> additionalTypesOverride = gameData.cloneOperation.additionalTypesOverride;
        List<ActivatedAbility> additionalActivatedAbilities = gameData.cloneOperation.additionalActivatedAbilities;
        String nameOverride = gameData.cloneOperation.nameOverride;
        Set<CardSupertype> additionalSupertypesOverride = gameData.cloneOperation.additionalSupertypesOverride;
        Set<CardSupertype> removedSupertypesOverride = gameData.cloneOperation.removedSupertypesOverride;
        boolean addTypeAppropriateCounters = gameData.cloneOperation.addTypeAppropriateCounters;
        CardColor embalmColorOverride = gameData.cloneOperation.embalmColorOverride;
        CardSubtype embalmAddedSubtype = gameData.cloneOperation.embalmAddedSubtype;
        boolean embalmRemoveManaCost = gameData.cloneOperation.embalmRemoveManaCost;
        DynamicAmount additionalPlusOnePlusOneCounters = gameData.cloneOperation.additionalPlusOnePlusOneCounters;
        Set<Keyword> additionalKeywordsOverride = gameData.cloneOperation.additionalKeywordsOverride;
        boolean additionalCreatureOnlyCharacteristics = gameData.cloneOperation.additionalCreatureOnlyCharacteristics;
        Set<CardSubtype> additionalSubtypesOverride = gameData.cloneOperation.additionalSubtypesOverride;
        Map<EffectSlot, List<CardEffect>> additionalSlotEffects = gameData.cloneOperation.additionalSlotEffects;
        boolean copyColor = gameData.cloneOperation.copyColor;
        int xValue = gameData.cloneOperation.xValue;

        gameData.cloneOperation.card = null;
        gameData.cloneOperation.controllerId = null;
        gameData.cloneOperation.etbTargetId = null;
        gameData.cloneOperation.powerOverride = null;
        gameData.cloneOperation.toughnessOverride = null;
        gameData.cloneOperation.copyPowerToughnessFromSource = false;
        gameData.cloneOperation.additionalTypesOverride = Set.of();
        gameData.cloneOperation.additionalActivatedAbilities = List.of();
        gameData.cloneOperation.nameOverride = null;
        gameData.cloneOperation.additionalSupertypesOverride = Set.of();
        gameData.cloneOperation.removedSupertypesOverride = Set.of();
        gameData.cloneOperation.addTypeAppropriateCounters = false;
        gameData.cloneOperation.embalmColorOverride = null;
        gameData.cloneOperation.embalmAddedSubtype = null;
        gameData.cloneOperation.embalmRemoveManaCost = false;
        gameData.cloneOperation.additionalPlusOnePlusOneCounters = null;
        gameData.cloneOperation.additionalSupertypesOverride = Set.of();
        gameData.cloneOperation.additionalKeywordsOverride = Set.of();
        gameData.cloneOperation.additionalCreatureOnlyCharacteristics = false;
        gameData.cloneOperation.additionalSubtypesOverride = Set.of();
        gameData.cloneOperation.additionalSlotEffects = Map.of();
        gameData.cloneOperation.copyColor = true;
        gameData.cloneOperation.xValue = 0;

        Permanent perm = new Permanent(card);

        if (targetId != null) {
            Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetId);
            if (targetPerm != null) {
                Integer effectivePowerOverride = copyPowerToughnessFromSource ? card.getPower() : powerOverride;
                Integer effectiveToughnessOverride = copyPowerToughnessFromSource ? card.getToughness() : toughnessOverride;
                permanentCopierService.applyCloneCopy(
                        perm, targetPerm.getCard(), effectivePowerOverride, effectiveToughnessOverride,
                        additionalTypesOverride, List.of(), copyColor);
                boolean creatureOnlyCharacteristicsApply = !additionalCreatureOnlyCharacteristics
                        || perm.getCard().hasType(CardType.CREATURE);
                applyAdditionalCopyCharacteristics(perm.getCard(), additionalSupertypesOverride,
                        creatureOnlyCharacteristicsApply ? additionalKeywordsOverride : Set.of());
                // "except it has..." — add additional abilities to the copy (e.g. Evil Twin)
                for (ActivatedAbility extraAbility : additionalActivatedAbilities) {
                    perm.getCard().addActivatedAbility(extraAbility);
                }
                if (nameOverride != null) {
                    perm.getCard().setName(nameOverride);
                }
                applyAdditionalSupertypes(perm.getCard(), additionalSupertypesOverride);
                applyRemovedSupertypes(perm.getCard(), removedSupertypesOverride);
                // "except it's an [subtype] in addition to its other types and it has ..." (Phantasmal Image)
                applyAdditionalSubtypes(perm.getCard(), additionalSubtypesOverride);
                additionalSlotEffects.forEach((slot, effects) ->
                        effects.forEach(effect -> perm.getCard().addEffect(slot, effect)));
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
                if (creatureOnlyCharacteristicsApply) {
                    applyAdditionalPlusOnePlusOneCounters(gameData, controllerId, perm,
                            additionalPlusOnePlusOneCounters, xValue);
                }
                if (addTypeAppropriateCounters) {
                    applyTypeAppropriateCounters(gameData, controllerId, perm);
                }
            }
        }

        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm, xValue, false);

        String playerName = gameData.playerIdToName.get(controllerId);
        Card enteredCard = perm.getCard();
        if (targetId != null) {
            Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetId);
            if (targetPerm != null) {
                gameLogService.append(gameData, GameLog.builder()
                        .card(enteredCard)
                        .text(" enters the battlefield as a copy of ")
                        .card(targetPerm.getCard())
                        .text(" under " + playerName + "'s control.")
                        .build());
                log.info("Game {} - {} enters as copy of {} for {}", gameData.id, enteredCard.getName(),
                        targetPerm.getCard().getName(), playerName);
            } else {
                gameLogService.append(gameData, GameLog.entersBattlefieldUnder(enteredCard, playerName));
                log.info("Game {} - {} enters battlefield without copying for {}", gameData.id, enteredCard.getName(), playerName);
            }
        } else {
            gameLogService.append(gameData, GameLog.entersBattlefieldUnder(enteredCard, playerName));
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
        if (gameQueryService.cantHaveCountersForController(gameData, perm, controllerId)) return;
        int count = amountEvaluationService.evaluate(gameData, amount,
                new AmountContext(controllerId, perm, null, xValue, 0));
        if (count > 0) {
            count = gameQueryService.doublePlusOnePlusOneCounters(gameData, perm, controllerId, count);
            if (count > 0) {
                perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                        perm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + count);
                log.info("Game {} - {} enters as copy with {} additional +1/+1 counter(s)",
                        gameData.id, perm.getCard().getName(), count);
            }
        }
    }

    private void applyAdditionalCopyCharacteristics(Card copy, Set<CardSupertype> additionalSupertypes,
                                                    Set<Keyword> additionalKeywords) {
        if (additionalSupertypes != null && !additionalSupertypes.isEmpty()) {
            EnumSet<CardSupertype> supertypes = EnumSet.noneOf(CardSupertype.class);
            supertypes.addAll(copy.getSupertypes());
            supertypes.addAll(additionalSupertypes);
            copy.setSupertypes(supertypes);
        }
        if (additionalKeywords != null && !additionalKeywords.isEmpty()) {
            EnumSet<Keyword> keywords = EnumSet.noneOf(Keyword.class);
            keywords.addAll(copy.getKeywords());
            keywords.addAll(additionalKeywords);
            copy.setKeywords(keywords);
        }
    }

    private void applyAdditionalSubtypes(Card copy, Set<CardSubtype> additionalSubtypes) {
        if (additionalSubtypes == null || additionalSubtypes.isEmpty()) return;
        List<CardSubtype> subtypes = new ArrayList<>(copy.getSubtypes());
        for (CardSubtype subtype : additionalSubtypes) {
            if (!subtypes.contains(subtype)) {
                subtypes.add(subtype);
            }
        }
        copy.setSubtypes(subtypes);
    }

    private void applyRemovedSupertypes(Card copy, Set<CardSupertype> removedSupertypes) {
        if (removedSupertypes == null || removedSupertypes.isEmpty()) return;
        Set<CardSupertype> supertypes = EnumSet.noneOf(CardSupertype.class);
        supertypes.addAll(copy.getSupertypes());
        supertypes.removeAll(removedSupertypes);
        copy.setSupertypes(supertypes);
    }

    private void applyTypeAppropriateCounters(GameData gameData, UUID controllerId, Permanent perm) {
        if (perm.getCard().hasType(CardType.CREATURE)) {
            applyAdditionalPlusOnePlusOneCounters(gameData, controllerId, perm,
                    new Fixed(1), 0);
        }
        if (perm.getCard().hasType(CardType.PLANESWALKER)) {
            applyAdditionalLoyaltyCounter(gameData, controllerId, perm);
        }
    }

    private void applyAdditionalLoyaltyCounter(GameData gameData, UUID controllerId, Permanent perm) {
        if (gameQueryService.cantHaveCountersForController(gameData, perm, controllerId)) return;
        int printedLoyalty = perm.getCard().getLoyalty() != null ? perm.getCard().getLoyalty() : 0;
        int count = gameQueryService.replaceCounters(gameData, perm, controllerId,
                CounterType.LOYALTY, printedLoyalty + 1);
        perm.setCounterCount(CounterType.LOYALTY, count);
    }

    private void applyAdditionalSupertypes(Card copy, Set<CardSupertype> additionalSupertypes) {
        if (additionalSupertypes == null || additionalSupertypes.isEmpty()) return;
        Set<CardSupertype> supertypes = EnumSet.noneOf(CardSupertype.class);
        supertypes.addAll(copy.getSupertypes());
        supertypes.addAll(additionalSupertypes);
        copy.setSupertypes(supertypes);
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
