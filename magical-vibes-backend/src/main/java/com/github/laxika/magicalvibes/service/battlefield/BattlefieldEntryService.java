package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TargetFilter;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseAnotherCreatureOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CantHaveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureMaxPowerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureMinPowerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithPlusOnePlusOneCountersPerSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardEnterWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedUnlessControlLandSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedUnlessFewLandsEffect;
import com.github.laxika.magicalvibes.model.effect.ReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesEnterAsCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintedCardNameMatchesEnteringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.KickedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseGameIfNotCastFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsAnotherSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MorbidConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentEnteredThisTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutPhylacteryCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class BattlefieldEntryService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final CardViewFactory cardViewFactory;
    private TargetLegalityService targetLegalityService;
    // @Lazy to break circular dependency:
    // BattlefieldEntryService → CloneService → BattlefieldEntryService
    private CloneService cloneService;

    public BattlefieldEntryService(GameQueryService gameQueryService,
                                    GameBroadcastService gameBroadcastService,
                                    PlayerInputService playerInputService,
                                    CardViewFactory cardViewFactory,
                                    TargetLegalityService targetLegalityService,
                                    @Lazy CloneService cloneService) {
        this.gameQueryService = gameQueryService;
        this.gameBroadcastService = gameBroadcastService;
        this.playerInputService = playerInputService;
        this.cardViewFactory = cardViewFactory;
        this.targetLegalityService = targetLegalityService;
        this.cloneService = cloneService;
    }

    /**
     * Sets the CloneService for manual (non-Spring) construction where
     * the circular dependency prevents passing it in the constructor.
     */
    public void setCloneService(CloneService cloneService) {
        this.cloneService = cloneService;
    }

    /**
     * Sets the TargetLegalityService for manual (non-Spring) construction
     * where initialization order prevents passing it in the constructor.
     */
    public void setTargetLegalityService(TargetLegalityService targetLegalityService) {
        this.targetLegalityService = targetLegalityService;
    }

    // ===== Permanent entry =====

    public void putPermanentOntoBattlefield(GameData gameData, UUID controllerId, Permanent permanent) {
        putPermanentOntoBattlefield(gameData, controllerId, permanent, snapshotEnterTappedTypes(gameData), List.of());
    }

    public void putPermanentOntoBattlefield(GameData gameData, UUID controllerId, Permanent permanent, Set<CardType> enterTappedTypes) {
        putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes, List.of());
    }

    /**
     * Core battlefield entry method. All overloads delegate here.
     *
     * @param simultaneouslyEntered permanents already placed on the battlefield as part of the
     *                              same simultaneous batch (e.g. mass reanimation) that must be
     *                              <em>excluded</em> from the CR 614.12 lookahead; may be empty
     */
    public void putPermanentOntoBattlefield(GameData gameData, UUID controllerId, Permanent permanent,
                                             Set<CardType> enterTappedTypes, List<Permanent> simultaneouslyEntered) {
        applyCreaturesEnterAsCopyReplacementEffect(gameData, controllerId, permanent);
        applyEnterTappedEffects(permanent, enterTappedTypes);
        applySelfEnterTapped(permanent);
        applyConditionalEnterTapped(gameData, controllerId, permanent);
        applyAllPermanentsEnterTapped(gameData, permanent);
        applyOpponentOnlyEnterTappedEffects(gameData, controllerId, permanent);
        applyEnterWithPlusOnePlusOneCountersPerSubtype(gameData, controllerId, permanent);
        applyGraveyardEnterWithAdditionalCounters(gameData, controllerId, permanent, simultaneouslyEntered);
        gameData.playerBattlefields.get(controllerId).add(permanent);
        gameData.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(controllerId, k -> new ArrayList<>())
                .add(permanent.getCard());
    }

    /**
     * CR 614.1c — "Creatures you control enter as a copy of this creature."
     * If the entering permanent is a creature and the controller has a permanent with
     * {@link CreaturesEnterAsCopyOfSourceEffect}, the entering creature becomes a copy
     * of that source permanent. This is mandatory (not a "may" ability).
     */
    private void applyCreaturesEnterAsCopyReplacementEffect(GameData gameData, UUID controllerId, Permanent entering) {
        if (!entering.getCard().hasType(CardType.CREATURE)) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        for (Permanent source : battlefield) {
            boolean hasEffect = source.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(e -> e instanceof CreaturesEnterAsCopyOfSourceEffect);
            if (hasEffect) {
                cloneService.applyCloneCopy(entering, source, null, null);
                // Reset any counters that were pre-set by the original card's "enters with"
                // replacement effects — the creature now enters as Essence, which has no such
                // effects, so those counters should not apply.
                entering.setPlusOnePlusOneCounters(0);
                entering.setChargeCounters(0);
                entering.setWishCounters(0);
                return;
            }
        }
    }

    public Set<CardType> snapshotEnterTappedTypes(GameData gameData) {
        Set<CardType> enterTappedTypes = EnumSet.noneOf(CardType.class);

        gameData.forEachPermanent((playerId, source) -> {
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                if (!(effect instanceof EnterPermanentsOfTypesTappedEffect enterTapped)) {
                    continue;
                }
                if (enterTapped.opponentsOnly()) {
                    continue;
                }
                enterTappedTypes.addAll(enterTapped.cardTypes());
            }
        });
        return enterTappedTypes;
    }

    private void applyEnterTappedEffects(Permanent enteringPermanent, Set<CardType> enterTappedTypes) {
        if (enterTappedTypes == null || enterTappedTypes.isEmpty()) {
            return;
        }
        if (matchesAnyType(enteringPermanent.getCard(), enterTappedTypes)) {
            enteringPermanent.tap();
        }
    }

    private void applyAllPermanentsEnterTapped(GameData gameData, Permanent enteringPermanent) {
        if (gameData.allPermanentsEnterTappedThisTurn) {
            enteringPermanent.tap();
        }
    }

    private void applyOpponentOnlyEnterTappedEffects(GameData gameData, UUID enteringControllerId, Permanent enteringPermanent) {
        gameData.forEachBattlefield((sourcePlayerId, battlefield) -> {
            if (sourcePlayerId.equals(enteringControllerId)) return;

            for (Permanent source : battlefield) {
                for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                    if (!(effect instanceof EnterPermanentsOfTypesTappedEffect enterTapped)) {
                        continue;
                    }
                    if (!enterTapped.opponentsOnly()) {
                        continue;
                    }
                    if (matchesAnyType(enteringPermanent.getCard(), enterTapped.cardTypes())) {
                        enteringPermanent.tap();
                    }
                }
            }
        });
    }

    private void applySelfEnterTapped(Permanent enteringPermanent) {
        boolean entersTapped = enteringPermanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof EntersTappedEffect);
        if (entersTapped) {
            enteringPermanent.tap();
        }
    }

    private void applyConditionalEnterTapped(GameData gameData, UUID controllerId, Permanent enteringPermanent) {
        for (CardEffect effect : enteringPermanent.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof EntersTappedUnlessFewLandsEffect fewLands) {
                // Count other lands the controller already has on the battlefield
                List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
                int otherLandCount = 0;
                if (battlefield != null) {
                    for (Permanent p : battlefield) {
                        if (p.getCard().hasType(CardType.LAND)) {
                            otherLandCount++;
                        }
                    }
                }
                if (otherLandCount > fewLands.maxOtherLands()) {
                    enteringPermanent.tap();
                }
            }
            if (effect instanceof EntersTappedUnlessControlLandSubtypeEffect checkLand) {
                // Check if the controller has a permanent with any of the required land subtypes
                List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
                boolean hasRequiredSubtype = false;
                if (battlefield != null) {
                    for (Permanent p : battlefield) {
                        for (CardSubtype subtype : checkLand.requiredSubtypes()) {
                            if (p.getCard().getSubtypes().contains(subtype)) {
                                hasRequiredSubtype = true;
                                break;
                            }
                        }
                        if (hasRequiredSubtype) break;
                    }
                }
                if (!hasRequiredSubtype) {
                    enteringPermanent.tap();
                }
            }
        }
    }

    private boolean matchesAnyType(Card card, Set<CardType> types) {
        if (types.contains(card.getType())) {
            return true;
        }
        for (CardType additionalType : card.getAdditionalTypes()) {
            if (types.contains(additionalType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Replacement effect (MTG Rule 614.1c): "This creature enters the battlefield with a +1/+1
     * counter on it for each other [subtype] you control and each [subtype] card in your graveyard."
     * Counts other permanents with the subtype on the controller's battlefield plus (optionally)
     * cards with the subtype in the controller's graveyard. (e.g. Unbreathing Horde)
     */
    private void applyEnterWithPlusOnePlusOneCountersPerSubtype(GameData gameData, UUID controllerId,
                                                                  Permanent permanent) {
        var effect = permanent.getCard().getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(e -> e instanceof EnterWithPlusOnePlusOneCountersPerSubtypeEffect)
                .map(e -> (EnterWithPlusOnePlusOneCountersPerSubtypeEffect) e)
                .findFirst().orElse(null);
        if (effect == null) return;

        boolean cantHaveCounters = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof CantHaveCountersEffect);
        if (cantHaveCounters) return;

        CardSubtype subtype = effect.subtype();
        int count = 0;

        // Count other permanents with the subtype on the controller's battlefield
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getCard().getSubtypes().contains(subtype)
                        || p.getTransientSubtypes().contains(subtype)
                        || p.getGrantedSubtypes().contains(subtype)
                        || p.hasKeyword(Keyword.CHANGELING)) {
                    count++;
                }
            }
        }

        // Count cards with the subtype in the controller's graveyard
        if (effect.includeGraveyard()) {
            List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
            if (graveyard != null) {
                for (Card card : graveyard) {
                    if (card.getSubtypes().contains(subtype)
                            || card.getKeywords().contains(Keyword.CHANGELING)) {
                        count++;
                    }
                }
            }
        }

        if (count > 0) {
            permanent.setPlusOnePlusOneCounters(permanent.getPlusOnePlusOneCounters() + count);
            log.info("Game {} - {} enters with {} +1/+1 counter(s) (per {} count)",
                    gameData.id, permanent.getCard().getName(), count, subtype);
        }
    }

    /**
     * Replacement effect (MTG Rule 614.1c): checks the controller's graveyard for cards with
     * {@link GraveyardEnterWithAdditionalCountersEffect} and adds +1/+1 counters to matching
     * creatures as they enter the battlefield. Uses CR 614.12 lookahead via
     * {@link GameQueryService#permanentWouldHaveSubtype} to determine subtypes.
     *
     * @param simultaneouslyEntered permanents to exclude from lookahead (see CR 614.12)
     */
    private void applyGraveyardEnterWithAdditionalCounters(GameData gameData, UUID controllerId,
                                                            Permanent permanent, List<Permanent> simultaneouslyEntered) {
        if (!permanent.getCard().hasType(CardType.CREATURE)) return;

        boolean cantHaveCounters = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof CantHaveCountersEffect);
        if (cantHaveCounters) return;

        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null || graveyard.isEmpty()) return;

        int additionalCounters = 0;
        for (Card card : graveyard) {
            for (CardEffect effect : card.getEffects(EffectSlot.STATIC)) {
                if (effect instanceof GraveyardEnterWithAdditionalCountersEffect graveyardEffect) {
                    if (gameQueryService.permanentWouldHaveSubtype(gameData, permanent, controllerId,
                            simultaneouslyEntered, graveyardEffect.subtype())) {
                        additionalCounters += graveyardEffect.count();
                    }
                }
            }
        }

        if (additionalCounters > 0) {
            permanent.setPlusOnePlusOneCounters(permanent.getPlusOnePlusOneCounters() + additionalCounters);
            log.info("Game {} - {} enters with {} additional +1/+1 counter(s) from graveyard effect(s)",
                    gameData.id, permanent.getCard().getName(), additionalCounters);
        }
    }

    // ===== ETB pipeline =====

    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand) {
        handleCreatureEnteredBattlefield(gameData, controllerId, card, targetId, wasCastFromHand, 0, false);
    }

    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode) {
        handleCreatureEnteredBattlefield(gameData, controllerId, card, targetId, wasCastFromHand, etbMode, false);
    }

    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode, boolean kicked) {
        // Track kicked status on the permanent for "if wasn't kicked" end-step triggers (e.g. Skizzik)
        if (kicked) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEnteredPerm = bf.get(bf.size() - 1);
            justEnteredPerm.setKicked(true);
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

        boolean needsColorChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof ChooseColorEffect);
        if (needsColorChoice) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            playerInputService.beginColorChoice(gameData, controllerId, justEntered.getId(), targetId);
            return;
        }

        processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand, etbMode, kicked);
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand) {
        processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand, 0, false);
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode) {
        processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand, etbMode, false);
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode, boolean kicked) {
        // Torpor Orb: "Creatures entering don't cause abilities to trigger."
        if (gameQueryService.areCreatureETBTriggersSuppressed(gameData, card)) {
            log.info("Game {} - {} ETB triggers suppressed (creature entering triggers disabled)", gameData.id, card.getName());
            return;
        }

        // Naban, Dean of Iteration: extra triggers when a Wizard enters
        int extraWizardTriggers = gameQueryService.countETBExtraTriggers(gameData, controllerId, card);

        List<CardEffect> triggeredEffects = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(e -> !(e instanceof ChooseColorEffect))
                .filter(e -> !(e instanceof ReplacementEffect))
                .toList();
        if (!triggeredEffects.isEmpty()) {
            // Extract per-mode targetFilter from ChooseOneEffect (if present)
            TargetFilter modeTargetFilter = null;
            for (CardEffect e : triggeredEffects) {
                if (e instanceof ChooseOneEffect coe) {
                    int idx = (etbMode >= 0 && etbMode < coe.options().size()) ? etbMode : 0;
                    modeTargetFilter = coe.options().get(idx).targetFilter();
                    break;
                }
            }

            List<CardEffect> mayEffects = triggeredEffects.stream().filter(e -> e instanceof MayEffect).toList();
            List<CardEffect> mandatoryEffects = triggeredEffects.stream()
                    .filter(e -> !(e instanceof MayEffect))
                    .map(e -> {
                        if (e instanceof LoseGameIfNotCastFromHandEffect) {
                            return wasCastFromHand ? null : new TargetPlayerLosesGameEffect(controllerId);
                        }
                        // Unwrap modal ETB choice (choose one) using the mode index from cast time
                        if (e instanceof ChooseOneEffect coe) {
                            if (etbMode >= 0 && etbMode < coe.options().size()) {
                                return coe.options().get(etbMode).effect();
                            }
                            return coe.options().getFirst().effect();
                        }
                        // Unwrap kicked conditional: if kicked, use inner effect; otherwise filter out
                        // (intervening-if — MTG Rule 603.4: ability doesn't trigger if condition not met)
                        if (e instanceof KickedConditionalEffect kce) {
                            return kicked ? kce.wrapped() : null;
                        }
                        return e;
                    })
                    .filter(Objects::nonNull)
                    // Conditional intervening-if: only trigger if condition is met
                    .filter(e -> {
                        if (e instanceof MetalcraftConditionalEffect) {
                            return gameQueryService.isMetalcraftMet(gameData, controllerId);
                        }
                        if (e instanceof MorbidConditionalEffect) {
                            return gameQueryService.isMorbidMet(gameData);
                        }
                        if (e instanceof ControlsAnotherSubtypeConditionalEffect cas) {
                            return gameQueryService.controlsAnotherSubtype(gameData, controllerId, card, cas.subtype());
                        }
                        return true;
                    })
                    .toList();

            for (CardEffect effect : mayEffects) {
                MayEffect may = (MayEffect) effect;
                List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                UUID sourcePermanentId = bf != null && !bf.isEmpty() ? bf.getLast().getId() : null;
                gameData.queueMayAbility(card, controllerId, may, null, sourcePermanentId);
                // Naban: extra triggers for Wizard ETB
                for (int i = 0; i < extraWizardTriggers; i++) {
                    gameData.queueMayAbility(card, controllerId, may, null, sourcePermanentId);
                }
            }

            if (!mandatoryEffects.isEmpty()) {
                // Separate graveyard exile effects (need multi-target selection at trigger time)
                List<CardEffect> graveyardExileEffects = mandatoryEffects.stream()
                        .filter(e -> e instanceof ExileCardsFromGraveyardEffect).toList();
                // Separate graveyard cast effects (need single-target selection at trigger time)
                List<CardEffect> graveyardCastEffects = mandatoryEffects.stream()
                        .filter(e -> e instanceof CastTargetInstantOrSorceryFromGraveyardEffect).toList();
                // Separate graveyard flashback-grant effects (need single-target selection at trigger time)
                List<CardEffect> graveyardFlashbackEffects = mandatoryEffects.stream()
                        .filter(e -> e instanceof GrantFlashbackToTargetGraveyardCardEffect).toList();
                List<CardEffect> otherEffects = mandatoryEffects.stream()
                        .filter(e -> !(e instanceof ExileCardsFromGraveyardEffect))
                        .filter(e -> !(e instanceof CastTargetInstantOrSorceryFromGraveyardEffect))
                        .filter(e -> !(e instanceof GrantFlashbackToTargetGraveyardCardEffect))
                        .filter(e -> !e.canTargetSpell()).toList();
                // Separate spell-targeting effects (need stack-target selection at trigger time)
                List<CardEffect> spellTargetEffects = mandatoryEffects.stream()
                        .filter(CardEffect::canTargetSpell).toList();

                // Put non-special effects on the stack as before
                if (!otherEffects.isEmpty()) {
                    if (!card.isNeedsTarget() || targetId != null) {
                        List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                        UUID sourcePermanentId = bf != null && !bf.isEmpty() ? bf.getLast().getId() : null;

                        StackEntry etbEntry = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                card,
                                controllerId,
                                card.getName() + "'s ETB ability",
                                new ArrayList<>(otherEffects),
                                0,
                                targetId,
                                sourcePermanentId,
                                Map.of(),
                                null,
                                List.of(),
                                List.of()
                        );
                        if (modeTargetFilter != null) {
                            etbEntry.setTargetFilter(modeTargetFilter);
                        }
                        gameData.stack.add(etbEntry);
                        String etbLog = card.getName() + "'s enter-the-battlefield ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, etbLog);
                        log.info("Game {} - {} ETB ability pushed onto stack", gameData.id, card.getName());
                        // Naban: extra triggers for Wizard ETB
                        for (int i = 0; i < extraWizardTriggers; i++) {
                            StackEntry extraEtbEntry = new StackEntry(
                                    StackEntryType.TRIGGERED_ABILITY,
                                    card,
                                    controllerId,
                                    card.getName() + "'s ETB ability",
                                    new ArrayList<>(otherEffects),
                                    0,
                                    targetId,
                                    sourcePermanentId,
                                    Map.of(),
                                    null,
                                    List.of(),
                                    List.of()
                            );
                            if (modeTargetFilter != null) {
                                extraEtbEntry.setTargetFilter(modeTargetFilter);
                            }
                            gameData.stack.add(extraEtbEntry);
                            gameBroadcastService.logAndBroadcast(gameData, etbLog);
                            log.info("Game {} - {} ETB ability pushed onto stack (Wizard ETB extra trigger)", gameData.id, card.getName());
                        }
                    }
                }

                // Handle graveyard exile effects: targets must be chosen at trigger time
                for (CardEffect effect : graveyardExileEffects) {
                    ExileCardsFromGraveyardEffect exile = (ExileCardsFromGraveyardEffect) effect;
                    for (int t = 0; t < 1 + extraWizardTriggers; t++) {
                        handleGraveyardExileETBTargeting(gameData, controllerId, card, mandatoryEffects, exile);
                    }
                }

                // Handle graveyard cast effects: target instant/sorcery in opponent's graveyard
                for (CardEffect effect : graveyardCastEffects) {
                    for (int t = 0; t < 1 + extraWizardTriggers; t++) {
                        handleGraveyardCastETBTargeting(gameData, controllerId, card, List.of(effect));
                    }
                }

                // Handle graveyard flashback-grant effects: target instant/sorcery in controller's graveyard
                for (CardEffect effect : graveyardFlashbackEffects) {
                    for (int t = 0; t < 1 + extraWizardTriggers; t++) {
                        handleGrantFlashbackETBTargeting(gameData, controllerId, card, List.of(effect));
                    }
                }

                // Handle spell-targeting ETB effects: target must be chosen from spells on the stack
                for (CardEffect effect : spellTargetEffects) {
                    StackEntryPredicate spellFilter = null;
                    if (effect instanceof CopySpellEffect cse) {
                        spellFilter = cse.spellFilter();
                    }
                    gameData.pendingETBSpellTargetTriggers.add(new PermanentChoiceContext.ETBSpellTargetTrigger(
                            card, controllerId, List.of(effect), spellFilter));
                }
                if (!gameData.pendingETBSpellTargetTriggers.isEmpty()
                        && !gameData.interaction.isAwaitingInput()) {
                    processNextETBSpellTargetTrigger(gameData);
                }
            }
        }

        checkAllyCreatureEntersTriggers(gameData, controllerId, card, extraWizardTriggers);
        checkAllyArtifactEntersTriggers(gameData, controllerId, card);
        checkAllyEquipmentEntersTriggers(gameData, controllerId, card);
        checkAllyNontokenArtifactEntersTriggers(gameData, controllerId, card);
        checkOpponentCreatureEntersTriggers(gameData, controllerId, card);
        checkAnyCreatureEntersTriggers(gameData, controllerId, card);
        if (card.hasType(CardType.LAND)) {
            checkOpponentLandEntersTriggers(gameData, controllerId, card);
            checkAllyLandEntersTriggers(gameData, controllerId, card);
        }
    }

    /**
     * Processes the next pending ETB trigger that targets a spell on the stack.
     * Collects valid spells matching the trigger's filter and presents a choice
     * to the controller. If no valid targets exist, the trigger is skipped.
     */
    public void processNextETBSpellTargetTrigger(GameData gameData) {
        while (!gameData.pendingETBSpellTargetTriggers.isEmpty()) {
            PermanentChoiceContext.ETBSpellTargetTrigger pending = gameData.pendingETBSpellTargetTriggers.peekFirst();

            // Collect valid spells from the stack
            List<UUID> validSpellCardIds = new ArrayList<>();
            for (StackEntry se : gameData.stack) {
                StackEntryType type = se.getEntryType();
                if (type != StackEntryType.INSTANT_SPELL && type != StackEntryType.SORCERY_SPELL
                        && type != StackEntryType.CREATURE_SPELL && type != StackEntryType.ENCHANTMENT_SPELL
                        && type != StackEntryType.ARTIFACT_SPELL && type != StackEntryType.PLANESWALKER_SPELL) {
                    continue; // skip triggered/activated abilities
                }
                if (pending.spellFilter() != null
                        && !targetLegalityService.matchesStackEntryPredicate(gameData, se, pending.spellFilter(), pending.controllerId())) {
                    continue;
                }
                validSpellCardIds.add(se.getCard().getId());
            }

            if (validSpellCardIds.isEmpty()) {
                gameData.pendingETBSpellTargetTriggers.removeFirst();
                String etbLog = pending.sourceCard().getName() + "'s enter-the-battlefield ability has no valid spell targets.";
                gameBroadcastService.logAndBroadcast(gameData, etbLog);
                log.info("Game {} - {} ETB spell-target trigger skipped (no valid targets)", gameData.id, pending.sourceCard().getName());
                continue;
            }

            gameData.pendingETBSpellTargetTriggers.removeFirst();
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(),
                    validSpellCardIds, List.of(),
                    pending.sourceCard().getName() + "'s ability — Choose target spell.");

            String logEntry = pending.sourceCard().getName() + "'s ETB ability triggers — choose a target spell.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} ETB spell-target trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    private void handleGraveyardExileETBTargeting(GameData gameData, UUID controllerId, Card card,
                                                   List<CardEffect> allEffects, ExileCardsFromGraveyardEffect exile) {
        // Collect all cards from all graveyards
        List<UUID> allCardIds = new ArrayList<>();
        List<CardView> allCardViews = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card graveyardCard : graveyard) {
                allCardIds.add(graveyardCard.getId());
                allCardViews.add(cardViewFactory.create(graveyardCard));
            }
        }

        if (allCardIds.isEmpty()) {
            // No graveyard cards: put ability on stack with 0 targets (just gains life on resolution)
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    controllerId,
                    card.getName() + "'s ETB ability",
                    new ArrayList<>(allEffects),
                    List.of()
            ));
            String etbLog = card.getName() + "'s enter-the-battlefield ability triggers.";
            gameBroadcastService.logAndBroadcast(gameData, etbLog);
            log.info("Game {} - {} ETB ability pushed onto stack with 0 targets (no graveyard cards)", gameData.id, card.getName());
        } else {
            // Prompt player to choose targets before putting ability on the stack
            int maxTargets = Math.min(exile.maxTargets(), allCardIds.size());
            gameData.graveyardTargetOperation.card = card;
            gameData.graveyardTargetOperation.controllerId = controllerId;
            gameData.graveyardTargetOperation.effects = new ArrayList<>(allEffects);
            playerInputService.beginMultiGraveyardChoice(gameData, controllerId, allCardIds, allCardViews, maxTargets,
                    "Choose up to " + maxTargets + " target card" + (maxTargets != 1 ? "s" : "") + " from graveyards to exile.");
        }
    }

    private void handleGraveyardCastETBTargeting(GameData gameData, UUID controllerId, Card card,
                                                  List<CardEffect> effects) {
        CastTargetInstantOrSorceryFromGraveyardEffect castEffect = effects.stream()
                .filter(e -> e instanceof CastTargetInstantOrSorceryFromGraveyardEffect)
                .map(e -> (CastTargetInstantOrSorceryFromGraveyardEffect) e)
                .findFirst().orElseThrow();
        GraveyardSearchScope scope = castEffect.scope();

        // Collect instant and sorcery cards from graveyards matching the scope
        List<UUID> eligibleCardIds = new ArrayList<>();
        List<CardView> cardViews = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            boolean include = switch (scope) {
                case OPPONENT_GRAVEYARD -> !playerId.equals(controllerId);
                case CONTROLLERS_GRAVEYARD -> playerId.equals(controllerId);
                case ALL_GRAVEYARDS -> true;
            };
            if (!include) continue;
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card graveyardCard : graveyard) {
                if (graveyardCard.hasType(CardType.INSTANT) || graveyardCard.hasType(CardType.SORCERY)) {
                    eligibleCardIds.add(graveyardCard.getId());
                    cardViews.add(cardViewFactory.create(graveyardCard));
                }
            }
        }

        if (eligibleCardIds.isEmpty()) {
            // No valid targets — trigger doesn't go on the stack
            String etbLog = card.getName() + "'s enter-the-battlefield ability has no valid targets.";
            gameBroadcastService.logAndBroadcast(gameData, etbLog);
            log.info("Game {} - {} ETB graveyard cast has no valid targets", gameData.id, card.getName());
        } else {
            // Prompt player to choose a target before putting ability on the stack
            gameData.graveyardTargetOperation.card = card;
            gameData.graveyardTargetOperation.controllerId = controllerId;
            gameData.graveyardTargetOperation.effects = new ArrayList<>(effects);
            playerInputService.beginMultiGraveyardChoice(gameData, controllerId, eligibleCardIds, cardViews, 1,
                    "Choose target instant or sorcery card from a graveyard to cast.");
        }
    }

    private void handleGrantFlashbackETBTargeting(GameData gameData, UUID controllerId, Card card,
                                                     List<CardEffect> effects) {
        GrantFlashbackToTargetGraveyardCardEffect flashbackEffect = effects.stream()
                .filter(e -> e instanceof GrantFlashbackToTargetGraveyardCardEffect)
                .map(e -> (GrantFlashbackToTargetGraveyardCardEffect) e)
                .findFirst().orElseThrow();

        // Collect eligible cards from controller's own graveyard
        List<UUID> eligibleCardIds = new ArrayList<>();
        List<CardView> cardViews = new ArrayList<>();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                boolean matchesType = false;
                for (CardType type : flashbackEffect.cardTypes()) {
                    if (graveyardCard.hasType(type)) {
                        matchesType = true;
                        break;
                    }
                }
                if (matchesType) {
                    eligibleCardIds.add(graveyardCard.getId());
                    cardViews.add(cardViewFactory.create(graveyardCard));
                }
            }
        }

        if (eligibleCardIds.isEmpty()) {
            // No valid targets — trigger doesn't go on the stack
            String etbLog = card.getName() + "'s enter-the-battlefield ability has no valid targets.";
            gameBroadcastService.logAndBroadcast(gameData, etbLog);
            log.info("Game {} - {} ETB grant flashback has no valid targets", gameData.id, card.getName());
        } else {
            // Prompt player to choose a target before putting ability on the stack
            gameData.graveyardTargetOperation.card = card;
            gameData.graveyardTargetOperation.controllerId = controllerId;
            gameData.graveyardTargetOperation.effects = new ArrayList<>(effects);
            playerInputService.beginMultiGraveyardChoice(gameData, controllerId, eligibleCardIds, cardViews, 1,
                    "Choose target instant or sorcery card in your graveyard to gain flashback.");
        }
    }

    // ===== Graveyard spell targeting helpers =====

    public void handleGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                       StackEntryType entryType, int xValue) {
        // Collect creature cards from controller's own graveyard
        List<UUID> creatureCardIds = new ArrayList<>();
        List<CardView> cardViews = new ArrayList<>();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (graveyardCard.hasType(CardType.CREATURE)) {
                    creatureCardIds.add(graveyardCard.getId());
                    cardViews.add(cardViewFactory.create(graveyardCard));
                }
            }
        }

        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = xValue;
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, creatureCardIds, cardViews, xValue,
                "Choose " + xValue + " target creature card" + (xValue != 1 ? "s" : "") + " from your graveyard to exile.");
    }

    public void handleAnyNumberGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                                StackEntryType entryType, CardPredicate filter) {
        List<UUID> matchingCardIds = new ArrayList<>();
        List<CardView> cardViews = new ArrayList<>();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (gameQueryService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCardIds.add(graveyardCard.getId());
                    cardViews.add(cardViewFactory.create(graveyardCard));
                }
            }
        }

        int maxTargets = matchingCardIds.size();
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.anyNumber = true;
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCardIds, cardViews, maxTargets,
                "Choose any number of target " + filterLabel + "s from your graveyard.");
    }

    public void handleUpToNGraveyardSpellTargeting(GameData gameData, UUID controllerId, Card card,
                                            StackEntryType entryType, CardPredicate filter, int maxTargetsCap,
                                            List<CardEffect> spellEffects) {
        List<UUID> matchingCardIds = new ArrayList<>();
        List<CardView> cardViews = new ArrayList<>();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (gameQueryService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCardIds.add(graveyardCard.getId());
                    cardViews.add(cardViewFactory.create(graveyardCard));
                }
            }
        }

        int maxTargets = Math.min(maxTargetsCap, matchingCardIds.size());
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(spellEffects);
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.anyNumber = true;
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCardIds, cardViews, maxTargets,
                "Choose up to " + maxTargetsCap + " target " + filterLabel + "s from your graveyard.");
    }

    public void handleUpToNTargetPlayerGraveyardSpellTargeting(GameData gameData, UUID controllerId,
                                            UUID targetPlayerId, Card card,
                                            StackEntryType entryType, CardPredicate filter, int maxTargetsCap,
                                            List<CardEffect> spellEffects) {
        List<UUID> matchingCardIds = new ArrayList<>();
        List<CardView> cardViews = new ArrayList<>();
        List<Card> graveyard = gameData.playerGraveyards.get(targetPlayerId);
        if (graveyard != null) {
            for (Card graveyardCard : graveyard) {
                if (gameQueryService.matchesCardPredicate(graveyardCard, filter, card.getId())) {
                    matchingCardIds.add(graveyardCard.getId());
                    cardViews.add(cardViewFactory.create(graveyardCard));
                }
            }
        }

        int maxTargets = Math.min(maxTargetsCap, matchingCardIds.size());
        gameData.graveyardTargetOperation.card = card;
        gameData.graveyardTargetOperation.controllerId = controllerId;
        gameData.graveyardTargetOperation.effects = new ArrayList<>(spellEffects);
        gameData.graveyardTargetOperation.entryType = entryType;
        gameData.graveyardTargetOperation.xValue = 0;
        gameData.graveyardTargetOperation.anyNumber = true;
        gameData.graveyardTargetOperation.targetPlayerId = targetPlayerId;
        String targetPlayerName = gameData.playerIdToName.get(targetPlayerId);
        String filterLabel = CardPredicateUtils.describeFilter(filter);
        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, matchingCardIds, cardViews, maxTargets,
                "Choose up to " + maxTargetsCap + " target " + filterLabel + "s from " + targetPlayerName + "'s graveyard.");
    }

    // ===== Enter triggers =====

    void checkAllyCreatureEntersTriggers(GameData gameData, UUID controllerId, Card enteringCreature, int extraWizardTriggers) {
        if (enteringCreature.getToughness() == null) return;

        int stackSizeBefore = gameData.stack.size();

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        for (Permanent perm : battlefield) {
            if (perm.getCard() == enteringCreature) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                if (effect instanceof EnteringCreatureMinPowerConditionalEffect conditional) {
                    if (enteringCreature.getPower() == null || enteringCreature.getPower() < conditional.minPower()) {
                        continue;
                    }
                    CardEffect innerEffect = conditional.wrapped();
                    if (innerEffect instanceof MayEffect may) {
                        gameData.queueMayAbility(perm.getCard(), controllerId, may);
                        String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} triggers for {} entering (power {} >= {})",
                                gameData.id, perm.getCard().getName(), enteringCreature.getName(),
                                enteringCreature.getPower(), conditional.minPower());
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                controllerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(List.of(innerEffect))
                        ));
                        String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} triggers for {} entering (power {} >= {})",
                                gameData.id, perm.getCard().getName(), enteringCreature.getName(),
                                enteringCreature.getPower(), conditional.minPower());
                    }
                } else if (effect instanceof EnteringCreatureMaxPowerConditionalEffect conditional) {
                    if (enteringCreature.getPower() == null || enteringCreature.getPower() > conditional.maxPower()) {
                        continue;
                    }
                    CardEffect innerEffect = conditional.wrapped();
                    if (innerEffect instanceof MayPayManaEffect mayPay) {
                        gameData.queueMayAbility(perm.getCard(), controllerId, mayPay, null);
                        String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} triggers for {} entering (power {} <= {})",
                                gameData.id, perm.getCard().getName(), enteringCreature.getName(),
                                enteringCreature.getPower(), conditional.maxPower());
                    } else if (innerEffect instanceof MayEffect may) {
                        gameData.queueMayAbility(perm.getCard(), controllerId, may);
                        String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} triggers for {} entering (power {} <= {})",
                                gameData.id, perm.getCard().getName(), enteringCreature.getName(),
                                enteringCreature.getPower(), conditional.maxPower());
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                controllerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(List.of(innerEffect))
                        ));
                        String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} triggers for {} entering (power {} <= {})",
                                gameData.id, perm.getCard().getName(), enteringCreature.getName(),
                                enteringCreature.getPower(), conditional.maxPower());
                    }
                } else if (effect instanceof SubtypeConditionalEffect conditional) {
                    if (!enteringCreature.getSubtypes().contains(conditional.subtype())) {
                        continue;
                    }
                    CardEffect innerEffect = conditional.wrapped();
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            controllerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(innerEffect)),
                            null,
                            perm.getId()
                    ));
                    String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers for {} entering (subtype {})",
                            gameData.id, perm.getCard().getName(), enteringCreature.getName(),
                            conditional.subtype());
                } else if (effect instanceof GainLifeEqualToToughnessEffect) {
                    int toughness = enteringCreature.getToughness();
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            controllerId,
                            perm.getCard().getName() + "'s ability",
                            List.of(new GainLifeEffect(toughness))
                    ));
                    String triggerLog = perm.getCard().getName() + " triggers — " +
                            gameData.playerIdToName.get(controllerId) + " will gain " + toughness + " life.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers for {} entering (toughness={})",
                            gameData.id, perm.getCard().getName(), enteringCreature.getName(), toughness);
                } else if (effect instanceof MayEffect may) {
                    gameData.queueMayAbility(perm.getCard(), controllerId, may);
                    String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers for {} entering (may effect)",
                            gameData.id, perm.getCard().getName(), enteringCreature.getName());
                }
            }
        }

        // Naban: duplicate all triggers that were just added for Wizard ETB
        if (extraWizardTriggers > 0) {
            List<StackEntry> newEntries = new ArrayList<>(gameData.stack.subList(stackSizeBefore, gameData.stack.size()));
            for (int i = 0; i < extraWizardTriggers; i++) {
                for (StackEntry entry : newEntries) {
                    gameData.stack.add(new StackEntry(entry));
                }
            }
        }
    }

    void checkAllyArtifactEntersTriggers(GameData gameData, UUID controllerId, Card enteringCard) {
        if (!enteringCard.hasType(CardType.ARTIFACT)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        for (Permanent perm : battlefield) {
            if (perm.getCard() == enteringCard) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
                String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} triggers for {} entering (ally artifact entered)",
                        gameData.id, perm.getCard().getName(), enteringCard.getName());
            }
        }
    }

    void checkAllyEquipmentEntersTriggers(GameData gameData, UUID controllerId, Card enteringCard) {
        if (!enteringCard.getSubtypes().contains(CardSubtype.EQUIPMENT)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        for (Permanent perm : battlefield) {
            if (perm.getCard() == enteringCard) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        controllerId,
                        perm.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(effect)),
                        null,
                        perm.getId()
                ));
                String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} triggers for {} entering (ally equipment entered)",
                        gameData.id, perm.getCard().getName(), enteringCard.getName());
            }
        }
    }

    void checkAllyNontokenArtifactEntersTriggers(GameData gameData, UUID controllerId, Card enteringCard) {
        if (enteringCard.isToken()) return;

        if (!enteringCard.hasType(CardType.ARTIFACT)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);

        // Find the entering permanent's ID
        UUID enteringPermanentId = null;
        for (Permanent p : battlefield) {
            if (p.getCard() == enteringCard) {
                enteringPermanentId = p.getId();
                break;
            }
        }

        for (Permanent perm : battlefield) {
            if (perm.getCard() == enteringCard) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            for (CardEffect effect : effects) {
                if (effect instanceof MayPayManaEffect mayPay) {
                    gameData.queueMayAbility(perm.getCard(), controllerId, mayPay, enteringPermanentId);
                    String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers for nontoken artifact {} entering",
                            gameData.id, perm.getCard().getName(), enteringCard.getName());
                } else if (effect instanceof MayEffect may) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            controllerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(effect)),
                            null,
                            perm.getId()
                    ));
                    String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers for nontoken artifact {} entering",
                            gameData.id, perm.getCard().getName(), enteringCard.getName());
                } else {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            controllerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(effect)),
                            null,
                            perm.getId()
                    ));
                    String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers for nontoken artifact {} entering",
                            gameData.id, perm.getCard().getName(), enteringCard.getName());
                }
            }
        }
    }

    void checkOpponentLandEntersTriggers(GameData gameData, UUID landControllerId, Card enteringLand) {
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(landControllerId)) return;

            for (Permanent perm : battlefield) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    CardEffect effectToResolve = effect;

                    if (effect instanceof PermanentEnteredThisTurnConditionalEffect conditional) {
                        List<Card> entered = gameData.permanentsEnteredBattlefieldThisTurn
                                .getOrDefault(landControllerId, List.of());
                        long matchCount = entered.stream()
                                .filter(c -> gameQueryService.matchesCardPredicate(c, conditional.predicate(), null))
                                .count();
                        if (matchCount < conditional.minCount()) continue;
                        effectToResolve = conditional.wrapped();
                    }

                    if (effect instanceof ImprintedCardNameMatchesEnteringPermanentConditionalEffect conditional) {
                        Card imprintedCard = perm.getCard().getImprintedCard();
                        if (imprintedCard == null || !imprintedCard.getName().equals(enteringLand.getName())) continue;
                        effectToResolve = conditional.wrapped();
                    }

                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            perm.getCard(),
                            playerId,
                            perm.getCard().getName() + "'s ability",
                            new ArrayList<>(List.of(effectToResolve)),
                            landControllerId,
                            perm.getId()
                    ));

                    String logEntry = perm.getCard().getName() + "'s ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} triggers on opponent land entering", gameData.id, perm.getCard().getName());
                }
            }
        });
    }

    void checkAllyLandEntersTriggers(GameData gameData, UUID landControllerId, Card enteringLand) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(landControllerId);
        for (Permanent perm : battlefield) {
            if (perm.getCard() == enteringLand) continue;

            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) continue;

            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    landControllerId,
                    perm.getCard().getName() + "'s ability",
                    new ArrayList<>(effects),
                    null,
                    perm.getId()
            ));

            String logEntry = perm.getCard().getName() + "'s ability triggers.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} triggers on ally land entering", gameData.id, perm.getCard().getName());
        }
    }

    void checkOpponentCreatureEntersTriggers(GameData gameData, UUID enteringCreatureControllerId, Card enteringCreature) {
        if (enteringCreature.getToughness() == null) return;

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(enteringCreatureControllerId)) return;

            for (Permanent perm : battlefield) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD);
                if (effects == null || effects.isEmpty()) continue;

                for (CardEffect effect : effects) {
                    if (effect instanceof MayEffect may) {
                        gameData.queueMayAbility(perm.getCard(), playerId, may, enteringCreatureControllerId, perm.getId());
                        String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} triggers for opponent creature {} entering",
                                gameData.id, perm.getCard().getName(), enteringCreature.getName());
                    } else {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s ability",
                                new ArrayList<>(List.of(effect)),
                                enteringCreatureControllerId,
                                perm.getId()
                        ));
                        String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} triggers for opponent creature {} entering",
                                gameData.id, perm.getCard().getName(), enteringCreature.getName());
                    }
                }
            }
        });
    }

    void checkAnyCreatureEntersTriggers(GameData gameData, UUID enteringCreatureControllerId, Card enteringCreature) {
        // Non-creature permanents (e.g. artifacts) should not trigger "creature enters" triggers
        if (enteringCreature.getToughness() == null) return;

        int extraWizardTriggers = gameQueryService.countETBExtraTriggers(gameData, enteringCreatureControllerId, enteringCreature);

        gameData.forEachPermanent((playerId, perm) -> {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD);
            if (effects == null || effects.isEmpty()) return;

            if (perm.getCard() == enteringCreature) return;

            // Naban: only doubles triggers on permanents controlled by the Wizard's controller
            int extraTriggers = playerId.equals(enteringCreatureControllerId) ? extraWizardTriggers : 0;

            for (CardEffect effect : effects) {
                if (effect instanceof GainLifeEffect gainLife) {
                    for (int t = 0; t < 1 + extraTriggers; t++) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                perm.getCard(),
                                playerId,
                                perm.getCard().getName() + "'s ability",
                                List.of(new GainLifeEffect(gainLife.amount()))
                        ));
                    }
                    String triggerLog = perm.getCard().getName() + " triggers — " +
                            gameData.playerIdToName.get(playerId) + " will gain " + gainLife.amount() + " life.";
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                    log.info("Game {} - {} triggers for {} entering (gain {} life)",
                            gameData.id, perm.getCard().getName(), enteringCreature.getName(), gainLife.amount());
                }
            }
        });
    }
}
