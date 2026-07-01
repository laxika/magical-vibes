package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetFilter;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CantHaveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CastFromZoneConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseAnotherCreatureOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsAnotherPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesEnterAsCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithPlusOnePlusOneCountersPerCreatureDeathsThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithPlusOnePlusOneCountersPerSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedUnlessControlsPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedUnlessFewLandsEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedUnlessManyLandsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardEnterWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.model.effect.KickedConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseGameIfNotCastFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MorbidConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.RaidConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class BattlefieldEntryService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final PermanentCopierService permanentCopierService;
    private final EnterTriggerScanService enterTriggerScanService;
    private final GraveyardTargetingService graveyardTargetingService;
    private final ETBTokenTargetService etbTokenTargetService;


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
        applyEnterWithPlusOnePlusOneCountersPerCreatureDeaths(gameData, permanent);
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
                permanentCopierService.applyCloneCopy(entering, source, null, null);
                // Reset any counters that were pre-set by the original card's "enters with"
                // replacement effects — the creature now enters as Essence, which has no such
                // effects, so those counters should not apply.
                entering.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
                entering.setCounterCount(CounterType.CHARGE, 0);
                entering.setCounterCount(CounterType.WISH, 0);
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
                int otherLandCount = countOtherLands(gameData, controllerId);
                if (otherLandCount > fewLands.maxOtherLands()) {
                    enteringPermanent.tap();
                }
            }
            if (effect instanceof EntersTappedUnlessManyLandsEffect manyLands) {
                int otherLandCount = countOtherLands(gameData, controllerId);
                if (otherLandCount < manyLands.minOtherLands()) {
                    enteringPermanent.tap();
                }
            }
            if (effect instanceof EntersTappedUnlessControlsPermanentEffect controlsPermanent) {
                if (!gameBroadcastService.controlsPermanent(gameData, controllerId, controlsPermanent.predicate())) {
                    enteringPermanent.tap();
                }
            }
        }
    }

    private int countOtherLands(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        int otherLandCount = 0;
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (p.getCard().hasType(CardType.LAND)) {
                    otherLandCount++;
                }
            }
        }
        return otherLandCount;
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
            permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + count);
            log.info("Game {} - {} enters with {} +1/+1 counter(s) (per {} count)",
                    gameData.id, permanent.getCard().getName(), count, subtype);
        }
    }

    /**
     * Replacement effect (MTG Rule 614.1c): "This creature enters with a +1/+1 counter on it
     * for each creature that died this turn."
     * Sums creature deaths across all players this turn. (e.g. Bloodcrazed Paladin)
     */
    private void applyEnterWithPlusOnePlusOneCountersPerCreatureDeaths(GameData gameData,
                                                                       Permanent permanent) {
        boolean hasEffect = permanent.getCard().getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof EnterWithPlusOnePlusOneCountersPerCreatureDeathsThisTurnEffect);
        if (!hasEffect) return;

        boolean cantHaveCounters = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof CantHaveCountersEffect);
        if (cantHaveCounters) return;

        int count = gameData.creatureDeathCountThisTurn.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        if (count > 0) {
            permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + count);
            log.info("Game {} - {} enters with {} +1/+1 counter(s) (creature deaths this turn)",
                    gameData.id, permanent.getCard().getName(), count);
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
            permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + additionalCounters);
            log.info("Game {} - {} enters with {} additional +1/+1 counter(s) from graveyard effect(s)",
                    gameData.id, permanent.getCard().getName(), additionalCounters);
        }
    }


    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand) {
        handleCreatureEnteredBattlefield(gameData, controllerId, card, targetId, wasCastFromHand, 0, false, List.of());
    }

    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode) {
        handleCreatureEnteredBattlefield(gameData, controllerId, card, targetId, wasCastFromHand, etbMode, false, List.of());
    }

    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode, boolean kicked) {
        handleCreatureEnteredBattlefield(gameData, controllerId, card, targetId, wasCastFromHand, etbMode, kicked, List.of());
    }

    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode, boolean kicked, List<UUID> targetIds) {
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

        processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand, etbMode, kicked, targetIds);
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand) {
        processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand, 0, false, List.of());
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, List<UUID> targetIds) {
        processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand, 0, false, targetIds);
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode) {
        processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand, etbMode, false, List.of());
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode, boolean kicked) {
        processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand, etbMode, kicked, List.of());
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode, boolean kicked, List<UUID> targetIds) {
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
                        // Unwrap cast-from-hand conditional: only fire if cast from hand
                        // (intervening-if — MTG Rule 603.4: e.g. "When this enters, if you cast it from your hand, [effect]")
                        if (e instanceof CastFromZoneConditionalEffect cfhce) {
                            return wasCastFromHand && cfhce.sourceZone() == Zone.HAND ? cfhce.wrapped() : null;
                        }
                        // "Gain life equal to that creature's toughness" — resolve toughness at trigger time
                        if (e instanceof GainLifeEqualToToughnessEffect) {
                            return new GainLifeEffect(card.getToughness());
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
                        if (e instanceof ControlsAnotherPermanentConditionalEffect capc) {
                            return gameQueryService.controlsAnotherPermanent(gameData, controllerId, card, capc.filter());
                        }
                        if (e instanceof RaidConditionalEffect) {
                            return gameData.playersDeclaredAttackersThisTurn.contains(controllerId);
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
                    boolean cardNeedsTarget = EffectResolution.needsTarget(card);
                    boolean hasTarget = targetId != null || !targetIds.isEmpty();

                    // A permanent that entered without a target chosen at cast time — a token copy,
                    // or a creature put onto the battlefield from a graveyard via undying,
                    // reanimation, etc. — must still choose targets for its mandatory ETB as the
                    // ability is put on the stack (CR 603.3b). Cast spells with "up to" targets that
                    // chose 0 targets are excluded; they passed through cast-time target selection.
                    List<Permanent> enteredBf = gameData.playerBattlefields.get(controllerId);
                    Permanent justEnteredPermanent = enteredBf != null && !enteredBf.isEmpty()
                            ? enteredBf.getLast() : null;
                    boolean enteredFromGraveyard = justEnteredPermanent != null
                            && justEnteredPermanent.getEnteredFromGraveyardOwnerId() != null;
                    boolean choosesTargetAtTriggerTime = card.isToken() || enteredFromGraveyard;

                    if (!cardNeedsTarget || hasTarget) {
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
                                targetIds != null ? targetIds : List.of()
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
                                    targetIds != null ? targetIds : List.of()
                            );
                            if (modeTargetFilter != null) {
                                extraEtbEntry.setTargetFilter(modeTargetFilter);
                            }
                            gameData.stack.add(extraEtbEntry);
                            gameBroadcastService.logAndBroadcast(gameData, etbLog);
                            log.info("Game {} - {} ETB ability pushed onto stack (Wizard ETB extra trigger)", gameData.id, card.getName());
                        }
                    } else if (choosesTargetAtTriggerTime) {
                        // CR 603.3: no target was chosen at cast time because the permanent
                        // wasn't cast (token copy, or returned from a graveyard via undying /
                        // reanimation). The controller must choose a target as the triggered
                        // ability is put on the stack.
                        // For non-token casts with "up to N" abilities where 0 was chosen,
                        // the ETB still triggers but has no effect — we skip queueing it.
                        List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                        UUID sourcePermanentId = bf != null && !bf.isEmpty() ? bf.getLast().getId() : null;

                        if (card.getSpellTargets().size() > 1 || etbTokenTargetService.hasGroupWithMaxTargetsGreaterThanOne(card)) {
                            // Multi-target ETB on a token copy (e.g. Burning Sun's Avatar, or a
                            // single group with "up to N" targets): choose slot-by-slot at
                            // trigger time, accumulating into targetIds.
                            gameData.pendingETBTokenMultiTargetTriggers.add(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                                    card, controllerId, new ArrayList<>(otherEffects), sourcePermanentId, List.of(), 0, 0));
                            for (int i = 0; i < extraWizardTriggers; i++) {
                                gameData.pendingETBTokenMultiTargetTriggers.add(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                                        card, controllerId, new ArrayList<>(otherEffects), sourcePermanentId, List.of(), 0, 0));
                            }
                            String etbLog = card.getName() + "'s enter-the-battlefield ability triggers — choose targets.";
                            gameBroadcastService.logAndBroadcast(gameData, etbLog);
                            log.info("Game {} - {} ETB multi-target trigger queued (no target chosen at cast time)",
                                    gameData.id, card.getName());
                        } else {
                            TargetFilter etbTargetFilter = modeTargetFilter != null ? modeTargetFilter : card.getTargetFilter();

                            gameData.pendingETBTokenTargetTriggers.add(new PermanentChoiceContext.ETBTokenTargetTrigger(
                                    card, controllerId, new ArrayList<>(otherEffects), sourcePermanentId, etbTargetFilter));
                            for (int i = 0; i < extraWizardTriggers; i++) {
                                gameData.pendingETBTokenTargetTriggers.add(new PermanentChoiceContext.ETBTokenTargetTrigger(
                                        card, controllerId, new ArrayList<>(otherEffects), sourcePermanentId, etbTargetFilter));
                            }
                            String etbLog = card.getName() + "'s enter-the-battlefield ability triggers — choose a target.";
                            gameBroadcastService.logAndBroadcast(gameData, etbLog);
                            log.info("Game {} - {} ETB trigger queued for target selection (no target chosen at cast time)",
                                    gameData.id, card.getName());
                        }
                    }
                }

                // Handle graveyard exile effects: targets must be chosen at trigger time
                for (CardEffect effect : graveyardExileEffects) {
                    ExileCardsFromGraveyardEffect exile = (ExileCardsFromGraveyardEffect) effect;
                    for (int t = 0; t < 1 + extraWizardTriggers; t++) {
                        graveyardTargetingService.handleGraveyardExileETBTargeting(gameData, controllerId, card, mandatoryEffects, exile);
                    }
                }

                // Handle graveyard cast effects: target instant/sorcery in opponent's graveyard
                for (CardEffect effect : graveyardCastEffects) {
                    for (int t = 0; t < 1 + extraWizardTriggers; t++) {
                        graveyardTargetingService.handleGraveyardCastETBTargeting(gameData, controllerId, card, List.of(effect));
                    }
                }

                // Handle graveyard flashback-grant effects: target instant/sorcery in controller's graveyard
                for (CardEffect effect : graveyardFlashbackEffects) {
                    for (int t = 0; t < 1 + extraWizardTriggers; t++) {
                        graveyardTargetingService.handleGrantFlashbackETBTargeting(gameData, controllerId, card, List.of(effect));
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
                    etbTokenTargetService.processNextETBSpellTargetTrigger(gameData);
                }
                if (!gameData.pendingETBTokenTargetTriggers.isEmpty()
                        && !gameData.interaction.isAwaitingInput()) {
                    etbTokenTargetService.processNextETBTokenTargetTrigger(gameData);
                }
                if (!gameData.pendingETBTokenMultiTargetTriggers.isEmpty()
                        && !gameData.interaction.isAwaitingInput()) {
                    etbTokenTargetService.processNextETBTokenMultiTargetTrigger(gameData);
                }
            }
        }

        enterTriggerScanService.checkAllyCreatureEntersTriggers(gameData, controllerId, card, extraWizardTriggers);
        enterTriggerScanService.checkAllyArtifactEntersTriggers(gameData, controllerId, card);
        enterTriggerScanService.checkAllyEquipmentEntersTriggers(gameData, controllerId, card);
        enterTriggerScanService.checkAllyNontokenArtifactEntersTriggers(gameData, controllerId, card);
        enterTriggerScanService.checkOpponentCreatureEntersTriggers(gameData, controllerId, card);
        enterTriggerScanService.checkAnyCreatureEntersTriggers(gameData, controllerId, card);
        enterTriggerScanService.checkEntersFromGraveyardTriggers(gameData, controllerId, card);
        if (card.hasType(CardType.LAND)) {
            enterTriggerScanService.checkOpponentLandEntersTriggers(gameData, controllerId, card);
            enterTriggerScanService.checkAllyLandEntersTriggers(gameData, controllerId, card);
        }
    }
}
