package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.effect.ExileAnyNumberOfCreatureCardsFromGraveyardOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ExileXCreatureCardsFromGraveyardOnEnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUpToXCreatureCardsFromGraveyardOnEnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.effect.ChooseAnotherCreatureOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseBasicLandTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseEquipmentAttachmentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePrimalClayFormOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.SubtypeChoiceOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessFormChoiceEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.MayReturnPermanentToHandAndEnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfLifeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DevourEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfCreaturesSetPowerToughnessOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsSetPowerToughnessToCountOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsAsEntersForCountersEffect;
import com.github.laxika.magicalvibes.model.effect.TurnOtherNontokenCreaturesFaceDownOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.TributeEffect;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.EquipSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class AsEntersInteractionService {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final EquipSupport equipSupport;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport permanentCounterSupport;
    private final EtbTriggerService etbTriggerService;

    @Autowired
    public AsEntersInteractionService(GameQueryService gameQueryService,
                                      PlayerInputService playerInputService,
                                      AmountEvaluationService amountEvaluationService,
                                      PredicateEvaluationService predicateEvaluationService,
                                      @Lazy EquipSupport equipSupport,
                                      @Lazy com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport permanentCounterSupport,
                                      @Lazy EtbTriggerService etbTriggerService) {
        this.gameQueryService = gameQueryService;
        this.playerInputService = playerInputService;
        this.amountEvaluationService = amountEvaluationService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.equipSupport = equipSupport;
        this.permanentCounterSupport = permanentCounterSupport;
        this.etbTriggerService = etbTriggerService;
    }

    public AsEntersInteractionService(GameQueryService gameQueryService,
                                      PlayerInputService playerInputService,
                                      AmountEvaluationService amountEvaluationService,
                                      PredicateEvaluationService predicateEvaluationService,
                                      @Lazy com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport permanentCounterSupport,
                                      @Lazy EtbTriggerService etbTriggerService) {
        this(gameQueryService, playerInputService, amountEvaluationService, predicateEvaluationService,
                null, permanentCounterSupport, etbTriggerService);
    }

    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand) {
        handleCreatureEnteredBattlefield(gameData, controllerId, card, targetId, wasCastFromHand, 0, false, List.of());
    }

    public void checkAllyTokenEntersTriggers(GameData gameData, UUID controllerId, int count) {
        etbTriggerService.checkAllyTokenEntersTriggers(gameData, controllerId, count);
    }

    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode) {
        handleCreatureEnteredBattlefield(gameData, controllerId, card, targetId, wasCastFromHand, etbMode, false, List.of());
    }

    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode, boolean kicked) {
        handleCreatureEnteredBattlefield(gameData, controllerId, card, targetId, wasCastFromHand, etbMode, kicked, List.of());
    }

    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode, boolean kicked, List<UUID> targetIds) {
        handleCreatureEnteredBattlefield(gameData, controllerId, card, targetId, wasCastFromHand,
                etbMode, etbMode, kicked, targetIds);
    }

    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetId,
                                                 boolean wasCastFromHand, int etbMode, int xValue,
                                                 boolean kicked, List<UUID> targetIds) {
        handleCreatureEnteredBattlefield(gameData, controllerId, card, targetId, wasCastFromHand,
                etbMode, xValue, kicked, targetIds, List.of());
    }

    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetId,
                                                 boolean wasCastFromHand, int etbMode, int xValue,
                                                 boolean kicked, List<UUID> targetIds,
                                                 List<String> repeatedAdditionalCosts) {
        handleCreatureEnteredBattlefield(gameData, controllerId, card, targetId, wasCastFromHand,
                etbMode, xValue, kicked, targetIds, repeatedAdditionalCosts, List.of());
    }

    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetId,
                                                 boolean wasCastFromHand, int etbMode, int xValue,
                                                 boolean kicked, List<UUID> targetIds,
                                                 List<String> repeatedAdditionalCosts,
                                                 List<UUID> convokeCreatureIds) {
        controllerId = resolveTokenControllerForEntry(gameData, controllerId, card);

        boolean turnsOtherCreaturesFaceDown = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(TurnOtherNontokenCreaturesFaceDownOnEnterEffect.class::isInstance);
        if (turnsOtherCreaturesFaceDown) {
            Permanent justEntered = gameData.playerBattlefields.get(controllerId).getLast();
            for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
                for (Permanent permanent : battlefield) {
                    if (permanent == justEntered || permanent.isFaceDown() || permanent.getCard().isToken()
                            || permanent.getOriginalCard().getBackFaceCard() != null
                            || !gameQueryService.isCreature(gameData, permanent)) {
                        continue;
                    }
                    permanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
                }
            }
        }

        // Track kicked status on the permanent for "if wasn't kicked" end-step triggers (e.g. Skizzik)
        if (kicked) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEnteredPerm = bf.get(bf.size() - 1);
            justEnteredPerm.setKicked(true);
        }

        // Tribute is chosen by an opponent as the creature enters. The choice is presented after
        // the permanent is placed so the shared may-ability interaction can carry it, but before
        // any ETB trigger is collected.
        TributeEffect tribute = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof TributeEffect)
                .map(TributeEffect.class::cast)
                .findFirst().orElse(null);
        if (tribute != null) {
            Permanent justEntered = gameData.playerBattlefields.get(controllerId).getLast();
            UUID opponentId = gameQueryService.getOpponentId(gameData, controllerId);
            if (opponentId != null) {
                gameData.pendingMayAbilities.add(new PendingMayAbility(
                        card,
                        controllerId,
                        List.of(tribute),
                        card.getName() + " — Put " + tribute.counterCount()
                                + " +1/+1 counter(s) on it to pay tribute?",
                        null,
                        null,
                        justEntered.getId(),
                        null,
                        0,
                        0,
                        null,
                        null,
                        opponentId,
                        null));
                playerInputService.processNextMayAbility(gameData);
                return;
            }
        }

        ChooseEquipmentAttachmentOnEnterEffect equipmentAttachment = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(ChooseEquipmentAttachmentOnEnterEffect.class::isInstance)
                .map(ChooseEquipmentAttachmentOnEnterEffect.class::cast)
                .findFirst().orElse(null);
        if (equipmentAttachment != null && equipSupport != null) {
            Permanent justEntered = gameData.playerBattlefields.get(controllerId).getLast();
            List<UUID> validIds = gameData.playerBattlefields.get(controllerId).stream()
                    .filter(permanent -> permanent != justEntered)
                    .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                    .filter(permanent -> equipSupport.canAttachEquipment(gameData, justEntered, permanent))
                    .map(Permanent::getId)
                    .toList();
            if (!validIds.isEmpty()) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.ChooseEquipmentToAttachAsEnter(
                                justEntered.getId(), controllerId, card, targetId, wasCastFromHand, etbMode,
                                xValue, kicked, targetIds, repeatedAdditionalCosts, convokeCreatureIds));
                playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                        "Choose a creature you control to attach it to.");
                return;
            }
        }

        // "As enters, choose another creature you control" — replacement effect (CR 614.1c),
        // not suppressed by Torpor Orb. Must happen before ETB triggers.
        boolean needsCreatureChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof ChooseAnotherCreatureOnEnterEffect);
        if (needsCreatureChoice) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            List<UUID> validIds = bf.stream()
                    .filter(p -> p != justEntered && gameQueryService.isCreature(gameData, p))
                    .map(Permanent::getId)
                    .toList();
            if (!validIds.isEmpty()) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.ChooseCreatureAsEnter(justEntered.getId(), controllerId, card, targetId, wasCastFromHand, etbMode, kicked));
                playerInputService.beginPermanentChoice(gameData, controllerId, new ArrayList<>(validIds), "Choose another creature you control.");
                return;
            }
            // No other creatures — bodyguard enters with no chosen creature
        }

        boolean needsPrimalClayFormChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof ChoosePrimalClayFormOnEnterEffect);
        if (needsPrimalClayFormChoice) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            playerInputService.beginPrimalClayFormChoice(gameData, controllerId, justEntered.getId());
            return;
        }

        PowerToughnessFormChoiceEffect powerToughnessChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(PowerToughnessFormChoiceEffect.class::isInstance)
                .map(PowerToughnessFormChoiceEffect.class::cast)
                .findFirst().orElse(null);
        if (powerToughnessChoice != null) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            playerInputService.beginPowerToughnessFormChoice(gameData, controllerId, justEntered.getId(),
                    powerToughnessChoice.forms(), false);
            return;
        }

        ChooseColorEffect colorChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(e -> e instanceof ChooseColorEffect)
                .map(e -> (ChooseColorEffect) e)
                .findFirst().orElse(null);
        if (colorChoice != null) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            playerInputService.beginColorChoice(gameData, controllerId, justEntered.getId(), targetId,
                    colorChoice);
            return;
        }

        ChooseModeOnEnterEffect modeChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(ChooseModeOnEnterEffect.class::isInstance)
                .map(ChooseModeOnEnterEffect.class::cast)
                .findFirst().orElse(null);
        if (modeChoice != null) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            if (justEntered.getChosenModeLabels().stream().noneMatch(modeChoice.modes()::contains)) {
                playerInputService.beginChooseModeOnEnterChoice(gameData, controllerId, card,
                        justEntered.getId(), modeChoice.modes());
                return;
            }
        }

        // "As this creature enters, choose a basic land type" — a choice made during entry
        // (CR 614.1c), before ETB triggers; the choice handler resumes them once made. Realmwright.
        ChooseBasicLandTypeOnEnterEffect landTypeChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(e -> e instanceof ChooseBasicLandTypeOnEnterEffect)
                .map(e -> (ChooseBasicLandTypeOnEnterEffect) e)
                .findFirst().orElse(null);
        if (landTypeChoice != null) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            playerInputService.beginBasicLandTypeChoice(gameData, controllerId, justEntered.getId(),
                    false, landTypeChoice.choicesRequired() > 1, landTypeChoice.allowedTypes());
            return;
        }

        // "As this creature enters, choose a creature type" — a choice made during entry
        // (CR 614.1c), before ETB triggers; the choice handler resumes them once made.
        SubtypeChoiceOnEnterEffect subtypeChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(SubtypeChoiceOnEnterEffect.class::isInstance)
                .map(SubtypeChoiceOnEnterEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (subtypeChoice != null) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            playerInputService.beginSubtypeChoice(gameData, controllerId, justEntered.getId(), subtypeChoice);
            return;
        }

        MayReturnPermanentToHandAndEnterWithCountersEffect returnChoice =
                card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                        .filter(MayReturnPermanentToHandAndEnterWithCountersEffect.class::isInstance)
                        .map(MayReturnPermanentToHandAndEnterWithCountersEffect.class::cast)
                        .findFirst().orElse(null);
        if (returnChoice != null) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = battlefield.getLast();
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(card.getId())
                    .withSourceControllerId(controllerId)
                    .withSourcePermanentId(justEntered.getId());
            boolean hasValidChoice = gameData.playerBattlefields.values().stream()
                    .flatMap(List::stream)
                    .anyMatch(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                            permanent, returnChoice.filter(), filterContext));
            if (hasValidChoice) {
                gameData.pendingMayAbilities.add(new PendingMayAbility(
                        card,
                        controllerId,
                        List.of(returnChoice),
                        card.getName() + " — Return " + returnChoice.permanentDescription()
                                + " you control to its owner's hand?",
                        targetId,
                        null,
                        justEntered.getId()));
                playerInputService.processNextMayAbility(gameData);
                return;
            }
        }

        // Devour (CR 702.82a): "As this creature enters, you may sacrifice any number of creatures.
        // It enters with N times that many +1/+1 counters on it." As-enters replacement, resolved
        // before ETB triggers. Prompt the controller to sacrifice any of their other creatures.
        DevourEffect devour = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(e -> e instanceof DevourEffect)
                .map(e -> (DevourEffect) e)
                .findFirst().orElse(null);
        if (devour != null) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            List<UUID> sacrificeable = bf.stream()
                    .filter(p -> p != justEntered && gameQueryService.isCreature(gameData, p))
                    .map(Permanent::getId)
                    .toList();
            if (!sacrificeable.isEmpty()) {
                playerInputService.beginMultiPermanentChoice(gameData, controllerId,
                        new ArrayList<>(sacrificeable), sacrificeable.size(),
                        new MultiPermanentChoiceContext.DevourSacrifice(justEntered.getId(), devour.multiplier(),
                                controllerId, card, targetId, wasCastFromHand, etbMode, kicked),
                        card.getName() + " — Devour: sacrifice any number of creatures.");
                return;
            }
            // No other creatures — devours nothing; ETB triggers proceed with 0 devoured creatures.
        }

        // "As this creature enters, sacrifice any number of creatures. This creature's power becomes
        // their total power and its toughness their total toughness" (CR 614.1c, Dracoplasm).
        boolean needsSacrificeForPowerToughness = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof SacrificeAnyNumberOfCreaturesSetPowerToughnessOnEnterEffect);
        if (needsSacrificeForPowerToughness) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            List<UUID> sacrificeable = bf.stream()
                    .filter(p -> p != justEntered && gameQueryService.isCreature(gameData, p))
                    .map(Permanent::getId)
                    .toList();
            if (!sacrificeable.isEmpty()) {
                playerInputService.beginMultiPermanentChoice(gameData, controllerId,
                        new ArrayList<>(sacrificeable), sacrificeable.size(),
                        new MultiPermanentChoiceContext.SacrificeCreaturesSetEnteringPowerToughness(
                                justEntered.getId(), controllerId, card, targetId, wasCastFromHand, etbMode, kicked),
                        card.getName() + " — sacrifice any number of creatures.");
                return;
            }
            // No other creatures — nothing is sacrificed; the creature enters as a 0/0.
        }

        SacrificeAnyNumberOfPermanentsSetPowerToughnessToCountOnEnterEffect sacrificeForPowerToughness =
                card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                        .filter(e -> e instanceof SacrificeAnyNumberOfPermanentsSetPowerToughnessToCountOnEnterEffect)
                        .map(SacrificeAnyNumberOfPermanentsSetPowerToughnessToCountOnEnterEffect.class::cast)
                        .findFirst().orElse(null);
        if (sacrificeForPowerToughness != null) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(card.getId())
                    .withSourceControllerId(controllerId);
            List<UUID> sacrificeable = bf.stream()
                    .filter(p -> p != justEntered)
                    .filter(p -> predicateEvaluationService.matchesPermanentPredicate(
                            p, sacrificeForPowerToughness.filter(), filterContext))
                    .map(Permanent::getId)
                    .toList();
            if (!sacrificeable.isEmpty()) {
                playerInputService.beginMultiPermanentChoice(gameData, controllerId,
                        new ArrayList<>(sacrificeable), sacrificeable.size(),
                        new MultiPermanentChoiceContext.SacrificePermanentsSetEnteringPowerToughness(
                                justEntered.getId(), sacrificeForPowerToughness.filter(), controllerId, card,
                                targetId, wasCastFromHand, etbMode, kicked),
                        card.getName() + " — sacrifice any number of permanents.");
                return;
            }
        }

        // "As this creature enters, sacrifice any number of permanents. It enters with that many
        // +1/+1 counters on it" (CR 614.1c, Shimatsu the Bloodcloaked). Resolved before ETB triggers;
        // the entering permanent itself isn't offered.
        SacrificePermanentsAsEntersForCountersEffect sacForCounters =
                card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                        .filter(e -> e instanceof SacrificePermanentsAsEntersForCountersEffect)
                        .map(e -> (SacrificePermanentsAsEntersForCountersEffect) e)
                        .findFirst().orElse(null);
        if (sacForCounters != null) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(card.getId())
                    .withSourceControllerId(controllerId);
            List<UUID> sacrificeable = bf.stream()
                    .filter(p -> p != justEntered)
                    .filter(p -> predicateEvaluationService.matchesPermanentPredicate(
                            p, sacForCounters.filter(), filterContext))
                    .map(Permanent::getId)
                    .toList();
            if (!sacrificeable.isEmpty()) {
                playerInputService.beginMultiPermanentChoice(gameData, controllerId,
                        new ArrayList<>(sacrificeable), sacrificeable.size(),
                        new MultiPermanentChoiceContext.SacrificeAsEntersForCounters(justEntered.getId(),
                                sacForCounters.countersPerPermanent(), controllerId, card, targetId,
                                wasCastFromHand, etbMode, kicked),
                        card.getName() + " — sacrifice any number of permanents.");
                return;
            }
            // Nothing to sacrifice — it enters with no counters; ETB triggers proceed.
        }

        // "As this creature enters, pay any amount of life" (Minion of the Wastes). The payment is a
        // choice made during entry, before ETB triggers; the amount is stored on the permanent so a
        // characteristic-defining power/toughness can read it back.
        PayAnyAmountOfLifeOnEnterEffect lifePayment = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(PayAnyAmountOfLifeOnEnterEffect.class::isInstance)
                .map(PayAnyAmountOfLifeOnEnterEffect.class::cast)
                .findFirst().orElse(null);
        if (lifePayment != null) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            int life = gameData.playerLifeTotals.getOrDefault(controllerId, 0);
            int maxLife = life;
            if (lifePayment.maxAmount() != null) {
                int maximum = amountEvaluationService.evaluate(gameData, lifePayment.maxAmount(),
                        AmountContext.forEnteringPermanent(controllerId, justEntered, xValue));
                maxLife = Math.min(maxLife, Math.max(0, maximum));
            }
            playerInputService.beginPayAnyAmountOfLifeChoice(gameData, controllerId, maxLife,
                    new ChoiceContext.PayAnyAmountOfLifeAsEnters(justEntered.getId(), controllerId, card,
                            targetId, wasCastFromHand, etbMode, kicked));
            return;
        }

        // "As this creature enters, exile any number of creature cards from your graveyard"
        // (CR 614.1c, Sutured Ghoul). The exiled cards are tracked with the entering permanent so
        // its characteristic-defining power/toughness can be derived from them.
        ExileUpToXCreatureCardsFromGraveyardOnEnterWithCountersEffect limitedGraveyardExile =
                card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                        .filter(ExileUpToXCreatureCardsFromGraveyardOnEnterWithCountersEffect.class::isInstance)
                        .map(ExileUpToXCreatureCardsFromGraveyardOnEnterWithCountersEffect.class::cast)
                        .findFirst().orElse(null);
        ExileXCreatureCardsFromGraveyardOnEnterWithCountersEffect requiredGraveyardExile =
                card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                        .filter(ExileXCreatureCardsFromGraveyardOnEnterWithCountersEffect.class::isInstance)
                        .map(ExileXCreatureCardsFromGraveyardOnEnterWithCountersEffect.class::cast)
                        .findFirst().orElse(null);
        boolean needsGraveyardExile = requiredGraveyardExile != null
                || limitedGraveyardExile != null
                || card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof ExileAnyNumberOfCreatureCardsFromGraveyardOnEnterEffect);
        if (needsGraveyardExile) {
            List<Card> creatureCards = gameData.playerGraveyards
                    .getOrDefault(controllerId, List.of()).stream()
                    .filter(c -> c.hasType(CardType.CREATURE))
                    .toList();
            int maxExiledCards = limitedGraveyardExile == null && requiredGraveyardExile == null
                    ? creatureCards.size()
                    : Math.min(Math.max(xValue, 0), creatureCards.size());
            if (!creatureCards.isEmpty() && maxExiledCards > 0) {
                List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                Permanent justEntered = bf.get(bf.size() - 1);
                gameData.graveyardTargetOperation.asEntersExile =
                        new GraveyardTargetOperationState.AsEntersGraveyardExileContext(
                                justEntered.getId(), controllerId, card, targetId, wasCastFromHand, etbMode,
                                xValue, kicked, targetIds,
                                limitedGraveyardExile == null ? 0 : limitedGraveyardExile.countersPerCard(),
                                requiredGraveyardExile == null ? List.of() : requiredGraveyardExile.counterTypes());
                playerInputService.beginMultiGraveyardChoice(gameData, controllerId,
                        new ArrayList<>(creatureCards), maxExiledCards,
                        requiredGraveyardExile == null ? 0 : maxExiledCards,
                        limitedGraveyardExile == null
                                ? card.getName() + " — Exile any number of creature cards from your graveyard."
                                : card.getName() + " — Exile up to " + xValue
                                + " creature cards from your graveyard.");
                return;
            }
        }

        etbTriggerService.processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand,
                etbMode, xValue, kicked, targetIds, repeatedAdditionalCosts, convokeCreatureIds);
    }

    public void applyAsEntersExileCounters(GameData gameData, UUID controllerId, UUID enteringPermanentId,
                                           int exiledCardCount, int countersPerCard) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, enteringPermanentId);
        if (permanent == null || exiledCardCount <= 0 || countersPerCard <= 0
                || gameQueryService.cantHaveCountersForController(gameData, permanent, controllerId)) {
            return;
        }
        int count = exiledCardCount * countersPerCard;
        count = gameQueryService.doublePlusOnePlusOneCounters(gameData, permanent, controllerId, count);
        if (count > 0) {
            permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                    permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + count);
            log.info("Game {} - {} enters with {} +1/+1 counter(s) from exiled creature cards",
                    gameData.id, permanent.getCard().getName(), count);
        }
    }

    public void applyAsEntersChosenCounterType(GameData gameData, UUID controllerId,
                                                UUID enteringPermanentId, CounterType counterType,
                                                int exiledCardCount) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, enteringPermanentId);
        if (permanent == null || exiledCardCount <= 0
                || gameQueryService.cantHaveCountersForController(gameData, permanent, controllerId)) {
            return;
        }

        int count = gameQueryService.replaceCounters(gameData, permanent, counterType, exiledCardCount);
        if (count <= 0) {
            return;
        }
        permanent.setCounterCount(counterType, permanent.getCounterCount(counterType) + count);
        permanentCounterSupport.recordCounterPlacedOnCreature(gameData, permanent, controllerId);
        if (counterType == CounterType.PLUS_ONE_PLUS_ONE) {
            permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
                    gameData, permanent, count, controllerId);
            permanentCounterSupport.firePlusOnePlusOneCounterTriggers(gameData, permanent);
        }
    }

    private UUID resolveTokenControllerForEntry(GameData gameData, UUID controllerId, Card card) {
        if (!card.isToken()) {
            return controllerId;
        }
        for (Map.Entry<UUID, List<Permanent>> battlefield : gameData.playerBattlefields.entrySet()) {
            if (battlefield.getValue().stream().anyMatch(permanent -> permanent.getCard() == card)) {
                return battlefield.getKey();
            }
        }
        return controllerId;
    }
}
