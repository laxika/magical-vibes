package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.ExilePlayCostModifier;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.SplitCardCast;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.action.ReturnExiledCardToHandAtNextEndStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import com.github.laxika.magicalvibes.service.cast.CastingPermissionService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.OutsideGameCards;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Casts a split card from a hand, graveyard, library or exile, or a revealed split card held outside
 * every zone, during resolution or through a "you may cast" permission. The player chooses the half as the spell is cast (CR 709.3), then that half's
 * targets (CR 601.2c), then pays that half's mana cost when the cast isn't free (CR 601.2h), before
 * the spell is put onto the stack. Only the chosen half is evaluated to see if it can be cast
 * (CR 709.3a), so a half is offered only if it fits the cast's mana value limit and has a legal
 * set of targets. Both halves may be cast together only from the caster's own hand, and only by a
 * card with fuse (CR 702.102a).
 */
@Slf4j
@Component
public class SplitCardCastSupport {

    static final String CHOOSE_TARGET = "Choose another target";
    static final String DONE = "Done";

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final TriggerCollectionService triggerCollectionService;
    private final ExileCastTargetSupport exileCastTargetSupport;
    private final DealDividedDamageSupport dealDividedDamageSupport;
    private final InputCompletionService inputCompletionService;
    private final ExileFreeCastQueueSupport exileFreeCastQueueSupport;
    private final GameQueryService gameQueryService;
    private final SpellCastingService spellCastingService;
    private final CopySupport copySupport;
    private final PermanentRemovalService permanentRemovalService;
    private final LifeSupport lifeSupport;
    private final CastingPermissionService castingPermissionService;
    private final GraveyardService graveyardService;
    private final MayCastHandlerService mayCastHandlerService;
    private final PotentialManaService potentialManaService;
    private final CastingCostService castingCostService;
    @org.springframework.beans.factory.annotation.Autowired
    @Lazy
    private com.github.laxika.magicalvibes.service.battlefield.DepartureService departureService;

    public SplitCardCastSupport(GameLogService gameLogService,
                                PlayerInputService playerInputService,
                                InteractionHandlerRegistry interactionHandlerRegistry,
                                TriggerCollectionService triggerCollectionService,
                                ExileCastTargetSupport exileCastTargetSupport,
                                DealDividedDamageSupport dealDividedDamageSupport,
                                @Lazy InputCompletionService inputCompletionService,
                                @Lazy ExileFreeCastQueueSupport exileFreeCastQueueSupport,
                                @Lazy GameQueryService gameQueryService,
                                @Lazy SpellCastingService spellCastingService,
                                CopySupport copySupport,
                                @Lazy PermanentRemovalService permanentRemovalService,
                                LifeSupport lifeSupport,
                                @Lazy CastingPermissionService castingPermissionService,
                                @Lazy GraveyardService graveyardService,
                                @Lazy MayCastHandlerService mayCastHandlerService,
                                @Lazy PotentialManaService potentialManaService,
                                @Lazy CastingCostService castingCostService) {
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
        this.triggerCollectionService = triggerCollectionService;
        this.exileCastTargetSupport = exileCastTargetSupport;
        this.dealDividedDamageSupport = dealDividedDamageSupport;
        this.inputCompletionService = inputCompletionService;
        this.exileFreeCastQueueSupport = exileFreeCastQueueSupport;
        this.gameQueryService = gameQueryService;
        this.spellCastingService = spellCastingService;
        this.copySupport = copySupport;
        this.permanentRemovalService = permanentRemovalService;
        this.lifeSupport = lifeSupport;
        this.castingPermissionService = castingPermissionService;
        this.graveyardService = graveyardService;
        this.mayCastHandlerService = mayCastHandlerService;
        this.potentialManaService = potentialManaService;
        this.castingCostService = castingCostService;
    }

    /**
     * Whether {@code card} is an instant or sorcery split card whose halves are the modes of its
     * spell. Rooms are split cards too, but they are cast as permanent spells with a door to unlock,
     * which this support doesn't cast.
     */
    public static boolean isSplitCard(Card card) {
        return card.getSplitHalves().size() >= 2 && splitModal(card) != null
                && !card.getSubtypes().contains(CardSubtype.ROOM);
    }

    /**
     * Whether {@code modeIndex} of a split card's modal spell casts both halves together, which
     * only a card with fuse cast from its owner's hand may do (CR 702.102a).
     */
    public static boolean isFusedMode(Card card, int modeIndex) {
        return isSplitCard(card) && modeIndex >= card.getSplitHalves().size();
    }

    /**
     * Offers the halves of {@code cast}'s card that can be cast. Returns false, leaving the card where
     * it was taken from, when none can.
     */
    public boolean begin(GameData gameData, SplitCardCast cast) {
        Card card = cast.physicalCard();
        ChooseOneEffect modal = splitModal(card);
        List<Integer> offered = new ArrayList<>();
        for (int modeIndex = 0; modeIndex < modal.options().size(); modeIndex++) {
            if (isCastableMode(gameData, cast, modal, modeIndex)) {
                offered.add(modeIndex);
            }
        }
        if (offered.isEmpty()) {
            returnToSourceZone(gameData, cast);
            return false;
        }

        List<String> labels = offered.stream().map(modal.options()::get)
                .map(ChooseOneEffect.ChooseOneOption::label).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                cast.controllerId(), null, null,
                new ChoiceContext.SplitFreeCastHalfChoice(cast, modal, offered),
                labels, card.getName() + " — Choose which half to cast."));
        return true;
    }

    /**
     * Casts the exiled split card {@code card} during resolution, paying the chosen half's mana cost
     * unless {@code payManaCost} is false. The card stays in exile until the spell is cast; the
     * exile permissions on it decide whether mana of any type or life equal to the half's mana
     * value pays that cost and where the spell goes once it resolves. Only a half whose mana value
     * is at most {@code maxManaValue} is offered. Returns false, without casting, when the card
     * can't be cast or no half can; the card then goes where {@code exileCast} sends an uncast card.
     */
    public boolean beginFromExile(GameData gameData, UUID controllerId, Card card, boolean payManaCost,
                                  SplitCardCast.ExileCast exileCast, int maxManaValue,
                                  CardEffect afterSuccessfulCastEffect, UUID sourcePermanentId) {
        UUID cardId = card.getId();
        ExiledCardEntry exiledEntry = gameData.findExiledCard(cardId);
        exileCast = exileCast
                .puttingOnLibraryBottom(exileCast.putOnLibraryBottomInsteadOfGraveyard()
                        || castingPermissionService.putsExileCastOnBottomOfOwnersLibrary(gameData, controllerId, cardId))
                .payingLifeEqualToManaValue(payManaCost
                        && castingPermissionService.hasExilePlayForLifeEqualToManaValuePermission(
                        gameData, controllerId, cardId));
        SplitCardCast cast = new SplitCardCast(controllerId, card, Zone.EXILE, 0,
                exiledEntry == null ? controllerId : exiledEntry.ownerId(),
                gameData.exileInsteadOfGraveyard.contains(cardId), maxManaValue,
                afterSuccessfulCastEffect, sourcePermanentId, payManaCost,
                castingPermissionService.hasAnyManaTypePermission(gameData, controllerId, cardId),
                0, false, null, exileCast, null);
        if (exiledEntry == null || !castingPermissionService.isSpellCastingAllowed(gameData, controllerId, card)) {
            returnToSourceZone(gameData, cast);
            return false;
        }
        return begin(gameData, cast);
    }

    /** Applies the chosen half, then asks for its targets or puts it onto the stack. */
    public void handleHalfChoice(GameData gameData, Player player, String chosenLabel,
                                 ChoiceContext.SplitFreeCastHalfChoice context) {
        SplitCardCast cast = context.cast();
        if (!player.getId().equals(cast.controllerId())) {
            throw new IllegalStateException("Not your choice");
        }
        Integer modeIndex = context.offeredModeIndices().stream()
                .filter(index -> context.effect().options().get(index).label().equals(chosenLabel))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid half: " + chosenLabel));
        gameData.interaction.clearAwaitingInput();

        ExileFreeCastQueueSupport.PreparedModalCast prepared = prepare(gameData, cast, context.effect(), modeIndex);
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " chooses \"" + chosenLabel + "\" for ", cast.physicalCard(), "."));
        SplitCardCast.Targeting targeting = new SplitCardCast.Targeting(cast, prepared.card(),
                exileCastTargetSupport.mapCardTypeToSpellType(prepared.card()), prepared.effects(),
                prepared.effectiveXValue(), modeEncoding(context.effect(), modeIndex), List.of());
        // A cost with {X} that will be paid has X announced now, before targets (CR 601.2b). A
        // free cast leaves X at 0 (CR 107.3b).
        String cost = costToPay(gameData, cast, prepared.card());
        if (cast.payManaCost() && cost != null && new ManaCost(cost).hasX()) {
            int maxX = Math.max(0, new ManaCost(cost).calculateMaxX(castingPool(gameData, cast),
                    costModifier(gameData, cast, prepared.card())));
            List<String> options = IntStream.rangeClosed(0, maxX).mapToObj(String::valueOf).toList();
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    cast.controllerId(), null, null, new ChoiceContext.SplitCastXValueChoice(targeting),
                    options, "Choose a value for X to cast " + prepared.card().getName() + "."));
            return;
        }
        continueAfterAnnouncements(gameData, targeting);
    }

    /** Applies the announced value of X, then asks for the half's targets or casts it. */
    public void handleXValueChoice(GameData gameData, Player player, String choice,
                                   ChoiceContext.SplitCastXValueChoice context) {
        SplitCardCast.Targeting targeting = context.targeting();
        if (!player.getId().equals(targeting.cast().controllerId())) {
            throw new IllegalStateException("Not your choice");
        }
        int xValue;
        try {
            xValue = Integer.parseInt(choice);
        } catch (NumberFormatException invalid) {
            throw new IllegalArgumentException("Invalid value for X: " + choice);
        }
        if (xValue < 0) {
            throw new IllegalArgumentException("Invalid value for X: " + choice);
        }
        gameData.interaction.clearAwaitingInput();
        continueAfterAnnouncements(gameData, new SplitCardCast.Targeting(targeting.cast(), targeting.cardToCast(),
                targeting.spellType(), targeting.spellEffects(), xValue, targeting.modeEncoding(),
                targeting.chosenTargets()));
    }

    private void continueAfterAnnouncements(GameData gameData, SplitCardCast.Targeting targeting) {
        if (!needsTargets(targeting.cardToCast(), targeting.spellEffects())) {
            putOnStack(gameData, targeting);
            return;
        }
        continueTargeting(gameData, targeting);
    }

    /** Records one chosen target, then asks for the next one or casts the spell. */
    public void handleTarget(GameData gameData, UUID targetId, PermanentChoiceContext.SplitFreeCastTarget context) {
        SplitCardCast.Targeting targeting = context.targeting();
        List<UUID> chosen = new ArrayList<>(targeting.chosenTargets());
        chosen.add(targetId);
        continueTargeting(gameData, new SplitCardCast.Targeting(targeting.cast(), targeting.cardToCast(),
                targeting.spellType(), targeting.spellEffects(), targeting.xValue(), targeting.modeEncoding(),
                chosen));
    }

    /** Applies the choice between taking another optional target and casting the spell now. */
    public void handleMoreTargetsChoice(GameData gameData, Player player, String choice,
                                        ChoiceContext.SplitFreeCastMoreTargetsChoice context) {
        SplitCardCast.Targeting targeting = context.targeting();
        if (!player.getId().equals(targeting.cast().controllerId())) {
            throw new IllegalStateException("Not your choice");
        }
        gameData.interaction.clearAwaitingInput();
        if (DONE.equals(choice)) {
            putOnStack(gameData, targeting);
        } else if (CHOOSE_TARGET.equals(choice)) {
            beginTargetChoice(gameData, targeting, context.candidates());
        } else {
            throw new IllegalArgumentException("Invalid choice: " + choice);
        }
    }

    /**
     * The modes of a split card's modal spell (its halves, then its fused spell) whose spell has the
     * characteristics {@code test} asks for. A permission to cast a spell with certain
     * characteristics considers only the half being cast (CR 709.3a); check while the card is still
     * in its zone, so counts such as the cards in hand are those the permission sees.
     */
    public List<Integer> modesMatching(GameData gameData, Card card, UUID controllerId, Predicate<Card> test) {
        ChooseOneEffect modal = splitModal(card);
        List<Integer> matching = new ArrayList<>();
        for (int modeIndex = 0; modeIndex < modal.options().size(); modeIndex++) {
            try {
                Card spell = exileFreeCastQueueSupport.prepareModalCast(
                        gameData, card, controllerId, modal, List.of(modeIndex)).card();
                if (test.test(spell)) {
                    matching.add(modeIndex);
                }
            } catch (IllegalStateException | IllegalArgumentException ignored) {
                // A mode that can't be prepared from here can't be cast from here either.
            }
        }
        return matching;
    }

    private boolean isCastableMode(GameData gameData, SplitCardCast cast, ChooseOneEffect modal, int modeIndex) {
        Card card = cast.physicalCard();
        if (cast.allowedModes() != null && !cast.allowedModes().contains(modeIndex)) {
            return false;
        }
        if (isFusedMode(card, modeIndex) && (!isFromOwnHand(cast) || !card.hasKeyword(Keyword.FUSE))) {
            return false;
        }
        try {
            ExileFreeCastQueueSupport.PreparedModalCast prepared = prepare(gameData, cast, modal, modeIndex);
            if (prepared.card().getManaValue() > cast.maxManaValue()) {
                return false;
            }
            if (cast.payManaCost() && !canPayCost(gameData, cast, prepared.card())) {
                return false;
            }
            return !needsTargets(prepared.card(), prepared.effects())
                    || hasLegalTargets(gameData, cast.controllerId(), prepared, modeEncoding(modal, modeIndex));
        } catch (IllegalStateException | IllegalArgumentException ignored) {
            return false;
        }
    }

    /**
     * The mana cost the cast pays for the prepared half: an alternative cost such as miracle, the
     * replacement cost an exile cast permits, or else the half's own mana cost.
     */
    private static String costToPay(GameData gameData, SplitCardCast cast, Card half) {
        if (cast.alternativeManaCost() != null) {
            return cast.alternativeManaCost();
        }
        if (cast.sourceZone() == Zone.EXILE) {
            ExilePlayCostModifier modifier = gameData.exilePlayCostModifiers.get(half.getId());
            if (modifier != null && cast.controllerId().equals(modifier.permittedPlayerId())
                    && modifier.replacementManaCost() != null) {
                return modifier.replacementManaCost();
            }
        }
        return half.getManaCost();
    }

    /**
     * Whether the caster could pay the cast's cost for the prepared half, X counting as 0, with the
     * mana in their pool and the mana their permanents can make, or with life when the cast pays
     * life equal to the half's mana value. A half that can't be paid for isn't offered (CR 601.2h).
     */
    private boolean canPayCost(GameData gameData, SplitCardCast cast, Card half) {
        UUID controllerId = cast.controllerId();
        SplitCardCast.ExileCast exileCast = cast.exileCast();
        if (exileCast != null && exileCast.payLifeEqualToManaValue()) {
            return gameData.getLife(controllerId) >= half.getManaValue()
                    && gameQueryService.canPlayerLifeChange(gameData, controllerId)
                    && gameQueryService.canPayLifeForCosts(gameData);
        }
        String cost = costToPay(gameData, cast, half);
        if (cost == null || cost.isBlank()) {
            return true;
        }
        return new ManaCost(cost).canPayWithAdditionalGenericCost(castingPool(gameData, cast), 0,
                costModifier(gameData, cast, half));
    }

    private ManaPool castingPool(GameData gameData, SplitCardCast cast) {
        ManaPool pool = potentialManaService.buildVirtualManaPool(gameData, cast.controllerId());
        if (cast.anyManaType()) {
            pool.setAllManaSpendableAsAnyColor(true);
        }
        return pool;
    }

    private int costModifier(GameData gameData, SplitCardCast cast, Card half) {
        Zone castZone = cast.revealedCast() != null ? Zone.LIBRARY : cast.sourceZone();
        return castingCostService.getCastCostModifier(gameData, cast.controllerId(), half, 0, castZone);
    }

    private ExileFreeCastQueueSupport.PreparedModalCast prepare(GameData gameData, SplitCardCast cast,
                                                                ChooseOneEffect modal, int modeIndex) {
        return exileFreeCastQueueSupport.prepareModalCast(
                gameData, cast.physicalCard(), cast.controllerId(), modal, List.of(modeIndex), isFromOwnHand(cast));
    }

    private static boolean isFromOwnHand(SplitCardCast cast) {
        return cast.sourceZone() == Zone.HAND && cast.ownerId().equals(cast.controllerId());
    }

    private boolean hasLegalTargets(GameData gameData, UUID controllerId,
                                    ExileFreeCastQueueSupport.PreparedModalCast prepared, int modeEncoding) {
        Card card = prepared.card();
        int minTargets = hasTargetSlots(card) ? card.getMinTargets() : 1;
        List<UUID> chosen = new ArrayList<>();
        for (int slot = 0; slot < minTargets; slot++) {
            List<UUID> candidates = slotCandidates(
                    gameData, card, prepared.effects(), controllerId, chosen, modeEncoding);
            if (candidates.isEmpty()) {
                return false;
            }
            chosen.add(candidates.getFirst());
        }
        return true;
    }

    private void continueTargeting(GameData gameData, SplitCardCast.Targeting targeting) {
        Card card = targeting.cardToCast();
        UUID controllerId = targeting.cast().controllerId();
        int chosenCount = targeting.chosenTargets().size();
        int minTargets = hasTargetSlots(card) ? card.getMinTargets() : 1;
        int maxTargets = hasTargetSlots(card) ? card.getMaxTargets() : 1;
        if (chosenCount >= maxTargets) {
            putOnStack(gameData, targeting);
            return;
        }

        List<UUID> candidates = slotCandidates(gameData, card, targeting.spellEffects(), controllerId,
                targeting.chosenTargets(), targeting.modeEncoding());
        if (candidates.isEmpty()) {
            if (chosenCount >= minTargets) {
                putOnStack(gameData, targeting);
            } else {
                // Every half offered had a legal set of targets, so this happens only if a remaining
                // target vanished while the earlier ones were chosen.
                returnToSourceZone(gameData, targeting.cast());
                gameLogService.append(gameData, GameLog.cardThen(targeting.cast().physicalCard(),
                        " has no legal targets and can't be cast."));
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }

        if (chosenCount >= minTargets) {
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    controllerId, null, null,
                    new ChoiceContext.SplitFreeCastMoreTargetsChoice(targeting, candidates),
                    List.of(CHOOSE_TARGET, DONE), "Choose another target for " + card.getName() + "?"));
            return;
        }
        beginTargetChoice(gameData, targeting, candidates);
    }

    /**
     * Legal candidates for the next target of the prepared half. A target group declared without a
     * filter (Fire's "one or two targets") takes whatever its effects can target.
     */
    private List<UUID> slotCandidates(GameData gameData, Card card, List<CardEffect> spellEffects,
                                      UUID controllerId, List<UUID> chosenTargets, int modeEncoding) {
        if (!hasTargetSlots(card)) {
            return exileCastTargetSupport.effectTargetCandidates(gameData, card, spellEffects, controllerId);
        }
        if (card.getMultiTargetFilters().get(chosenTargets.size()) == null) {
            List<UUID> candidates = new ArrayList<>(
                    exileCastTargetSupport.effectTargetCandidates(gameData, card, spellEffects, controllerId));
            if (!card.isAllowSharedTargets()) {
                candidates.removeAll(chosenTargets);
            }
            return candidates;
        }
        return exileCastTargetSupport.nextSlotCandidates(gameData, card, controllerId, chosenTargets, modeEncoding);
    }

    private void beginTargetChoice(GameData gameData, SplitCardCast.Targeting targeting, List<UUID> candidates) {
        playerInputService.beginPermanentChoice(gameData, targeting.cast().controllerId(), candidates,
                new PermanentChoiceContext.SplitFreeCastTarget(targeting),
                "Choose a target for " + targeting.cardToCast().getName() + ".");
    }

    /**
     * CR 601.2h and CR 616.1: before a prepared cast pays its mana cost, asks the death replacement questions of the
     * Lich's Duel Mastery that paying life for it would sacrifice instead (paying life is losing it, CR 119.4): life
     * equal to its mana value from exile, or Phyrexian mana paid with life
     * ({@link SpellCastingService#nonHandManaCostLifePayments}). When one is asked, nothing is paid and the cast is
     * put on the stack again once every answer is in ({@link #putOnStackAfterLifeCostAnswers}); the life payment's
     * sacrifice consumes the answers. Nothing is asked inside a rerun window, whose completion asks as the Mastery
     * leaves.
     *
     * @return {@code true} when the cast stopped to ask
     */
    private boolean askCastLifePaymentFirst(GameData gameData, SplitCardCast.Targeting targeting,
                                            SplitCardCast.ExileCast exileCast) {
        if (gameData.deathReplacementsCanRerunEffect) {
            return false;
        }
        SplitCardCast cast = targeting.cast();
        Card card = targeting.cardToCast();
        int payments = cast.sourceZone() == Zone.EXILE && exileCast != null && exileCast.payLifeEqualToManaValue()
                ? (card.getManaValue() > 0 ? 1 : 0)
                : spellCastingService.nonHandManaCostLifePayments(gameData, cast.controllerId(), card,
                        targeting.xValue(), cast.sourceZone() == Zone.EXILE ? null : cast.alternativeManaCost());
        List<com.github.laxika.magicalvibes.model.DepartureIntent> sacrifices = payments > 0
                ? lifeSupport.successiveLifeLossSacrifices(gameData, cast.controllerId(), payments) : List.of();
        if (sacrifices.isEmpty() || !departureService.askBeforePayment(gameData,
                com.github.laxika.magicalvibes.model.DepartureBatch.of(sacrifices))) {
            return false;
        }
        gameData.pendingDeathReplacementChoiceReplay = new GameData.SplitCastPaymentReplay(targeting);
        return true;
    }

    /**
     * Puts the prepared cast {@link #askCastLifePaymentFirst} stopped on the stack again, paying its cost, once every
     * death replacement it asked about is chosen.
     */
    public void putOnStackAfterLifeCostAnswers(GameData gameData, GameData.SplitCastPaymentReplay replay) {
        putOnStack(gameData, replay.targeting());
    }

    private void putOnStack(GameData gameData, SplitCardCast.Targeting targeting) {
        SplitCardCast cast = targeting.cast();
        Card card = targeting.cardToCast();
        UUID controllerId = cast.controllerId();
        List<UUID> targets = targeting.chosenTargets();
        List<CardEffect> effects = new ArrayList<>(targeting.spellEffects());
        if (cast.origin() instanceof SplitCardCast.WordOfCommandCast) {
            playFromHand(gameData, targeting);
            return;
        }
        SplitCardCast.ExileCast exileCast = cast.exileCast();
        ExiledCardEntry exiledEntry = null;
        if (cast.sourceZone() == Zone.EXILE) {
            exiledEntry = gameData.findExiledCard(cast.physicalCard().getId());
            if (exiledEntry == null) {
                gameLogService.append(gameData, GameLog.cardThen(cast.physicalCard(),
                        " is no longer in exile and can't be cast."));
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }
        }

        StackEntry entry;
        Map<UUID, Integer> damageAssignments = dividedDamageAssignments(gameData, targeting);
        if (damageAssignments != null) {
            entry = new StackEntry(targeting.spellType(), card, controllerId, card.getName(), effects,
                    targeting.xValue(), (UUID) null, damageAssignments);
        } else if (targets.size() == 1 && !hasTargetSlots(card)) {
            UUID targetId = targets.getFirst();
            Zone targetZone = gameQueryService.findCardInGraveyardById(gameData, targetId) != null ? Zone.GRAVEYARD
                    : gameData.stack.stream().anyMatch(stacked -> targetId.equals(stacked.getTargetableId()))
                    ? Zone.STACK : null;
            entry = new StackEntry(targeting.spellType(), card, controllerId, card.getName(), effects,
                    targeting.xValue(), targetId, null, null, targetZone, null, null);
        } else if (!targets.isEmpty()) {
            entry = new StackEntry(targeting.spellType(), card, controllerId, card.getName(), effects,
                    targeting.xValue(), targets);
        } else {
            entry = new StackEntry(targeting.spellType(), card, controllerId, card.getName(), effects,
                    targeting.xValue(), (UUID) null, null);
        }
        entry.setExileInsteadOfGraveyard(cast.exileInsteadOfGraveyard());
        entry.setSourceZone(cast.sourceZone());
        if (cast.sourceZone() == Zone.GRAVEYARD || cast.sourceZone() == Zone.OUTSIDE_GAME
                || !cast.ownerId().equals(controllerId)) {
            entry.setOwnerIdOverride(cast.ownerId());
        }
        if (exileCast != null) {
            entry.setCopy(exileCast.copy());
            entry.setCastCopy(exileCast.copy());
            entry.setPutOnBottomOfOwnersLibraryInsteadOfGraveyard(exileCast.putOnLibraryBottomInsteadOfGraveyard());
        }
        if (cast.payManaCost() && askCastLifePaymentFirst(gameData, targeting, exileCast)) {
            return;
        }
        if (cast.payManaCost()) {
            try {
                if (cast.sourceZone() == Zone.EXILE) {
                    spellCastingService.payResolutionExileCastCost(gameData, controllerId, card,
                            targeting.xValue(), cast.anyManaType(),
                            exileCast != null && exileCast.payLifeEqualToManaValue());
                } else {
                    spellCastingService.paySpellManaCostFromNonHandZone(gameData, controllerId, card,
                            targeting.xValue(), cast.sourceZone(), cast.anyManaType(), cast.alternativeManaCost());
                }
            } catch (IllegalStateException | IllegalArgumentException unpaid) {
                returnToSourceZone(gameData, cast);
                gameLogService.append(gameData, GameLog.cardThen(cast.physicalCard(),
                        " can't be cast because its mana cost can't be paid."));
                copySupport.flushDeferredGraveyardCastCopies(gameData);
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }
        } else if (cast.origin() instanceof SplitCardCast.OutsideGameCast outsideGame
                && outsideGame.paySideboardCastTax()) {
            if (!mayCastHandlerService.paySideboardRevealOrManaCosts(gameData, controllerId, card, false)) {
                returnToSourceZone(gameData, cast);
                gameLogService.append(gameData, GameLog.cardThen(cast.physicalCard(),
                        " cannot be cast because its additional cost cannot be paid."));
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }
            mayCastHandlerService.paySideboardRevealOrManaCosts(gameData, controllerId, card, true);
        }
        leaveSourceZone(gameData, cast);
        if (exileFreeCastQueueSupport.beginSacrificeCostIfNeeded(gameData, entry)) {
            return;
        }
        gameData.stack.add(entry);
        if (cast.restrictAdditionalSpellsThisTurn()) {
            gameData.preventAdditionalSpellCastsThisTurn(controllerId);
        }
        if (cast.sourceZone() == Zone.GRAVEYARD) {
            copySupport.copyGraveyardCastSpell(gameData, entry, card, controllerId, cast.copyCount());
        }
        gameData.recordSpellCast(controllerId, card);
        gameData.priorityPassedBy.clear();

        String playerName = gameData.playerIdToName.get(controllerId);
        String costPhrase = cast.payManaCost() ? "" : " without paying its mana cost";
        gameLogService.append(gameData, GameLog.textCardText(playerName + " casts ", card, costPhrase + "."));
        log.info("Game {} - {} casts {} from {}{}", gameData.id, playerName, card.getName(),
                cast.sourceZone(), costPhrase);

        UUID exiledSourcePermanentId = exiledEntry == null || exileCast == null || exileCast.copy()
                ? null : exiledEntry.sourcePermanentId();
        triggerCollectionService.checkSpellCastTriggers(gameData, card, controllerId, cast.sourceZone(),
                exiledSourcePermanentId);
        if (!targets.isEmpty()) {
            triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
        }
        exileCastTargetSupport.queueAfterSuccessfulCast(gameData, card, controllerId,
                cast.sourcePermanentId(), cast.afterSuccessfulCastEffect());
        if (exileCast != null && exileCast.lifeLossOnCast() > 0) {
            // "If you do, you lose N life" follows the cast as a step of its own (CR 608.2c, CR 616.1).
            lifeSupport.loseLifeAsStepOfParkedResolution(gameData,
                    Map.of(controllerId, exileCast.lifeLossOnCast()), card.getName());
        }
        if (cast.revealedCast() != null && cast.revealedCast().cardsToBottom() != null) {
            putOnLibraryBottom(gameData, cast.ownerId(), cast.revealedCast().cardsToBottom());
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Casts the chosen half of the card Word of Command has its owner play through the normal cast
     * from hand, with the half as the announced mode and the announced X, so that cast pays its cost
     * and does Word of Command's bookkeeping. A cast that fails leaves the card in hand.
     */
    private void playFromHand(GameData gameData, SplitCardCast.Targeting targeting) {
        SplitCardCast cast = targeting.cast();
        UUID controllerId = cast.controllerId();
        List<Card> hand = gameData.playerHands.getOrDefault(controllerId, List.of());
        int cardIndex = -1;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getId().equals(cast.physicalCard().getId())) {
                cardIndex = i;
                break;
            }
        }
        if (cardIndex < 0) {
            gameLogService.append(gameData, GameLog.cardThen(cast.physicalCard(), " is no longer in hand."));
            returnToSourceZone(gameData, cast);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        List<UUID> targets = targeting.chosenTargets();
        Map<UUID, Integer> damageAssignments = dividedDamageAssignments(gameData, targeting);
        boolean singleTarget = targets.size() == 1 && !hasTargetSlots(targeting.cardToCast());
        gameData.priorityPassedBy.clear();
        if (!controllerId.equals(gameData.activePlayerId)) {
            gameData.priorityPassedBy.add(gameData.activePlayerId);
        }
        try {
            spellCastingService.playCard(gameData, new Player(controllerId, gameData.playerIdToName.get(controllerId)),
                    cardIndex, targeting.modeEncoding(), singleTarget ? targets.getFirst() : null,
                    damageAssignments == null ? Map.of() : damageAssignments,
                    singleTarget ? List.of() : targets, List.of(), false, null, null, List.of(), null, null,
                    false, null, targeting.xValue());
        } catch (IllegalArgumentException | IllegalStateException failure) {
            log.info("Game {} - could not cast {} from hand: {}", gameData.id, targeting.cardToCast().getName(),
                    failure.getMessage());
            returnToSourceZone(gameData, cast);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }

    /**
     * The division of a "divided as you choose" damage half among its targets, announced as it is
     * cast (CR 601.2d), or null when the half divides no damage. Each target must be dealt at least
     * 1 damage, so the division is fixed with one target or with as many targets as damage.
     */
    private Map<UUID, Integer> dividedDamageAssignments(GameData gameData, SplitCardCast.Targeting targeting) {
        boolean dividesDamage = targeting.spellEffects().stream()
                .filter(DealDividedDamageEffect.class::isInstance)
                .map(DealDividedDamageEffect.class::cast)
                .anyMatch(effect -> effect.mode() == DivisionMode.CHOSEN && !effect.etbAssignments());
        List<UUID> targets = targeting.chosenTargets();
        if (!dividesDamage || targets.isEmpty()) {
            return null;
        }
        int total = dealDividedDamageSupport.damageAssignedToSingleTarget(gameData, targeting.spellEffects(),
                targeting.cast().controllerId(), targeting.xValue(), false);
        if (targets.size() != 1 && targets.size() != total) {
            throw new IllegalStateException("Choosing a damage division for " + targeting.cardToCast().getName()
                    + " is not supported");
        }
        Map<UUID, Integer> assignments = new LinkedHashMap<>();
        for (UUID target : targets) {
            assignments.put(target, targets.size() == 1 ? total : 1);
        }
        return assignments;
    }

    /**
     * Completes the card's move to the stack. The card was set aside while the half and its targets
     * were chosen, so a cast that fails leaves no trace; a card that leaves a graveyard is now
     * removed the usual way, so the game sees it leave. A card cast from exile stayed there and
     * leaves it now.
     */
    private void leaveSourceZone(GameData gameData, SplitCardCast cast) {
        UUID cardId = cast.physicalCard().getId();
        if (cast.sourceZone() == Zone.GRAVEYARD) {
            returnToSourceZone(gameData, cast);
            permanentRemovalService.removeCardFromGraveyardById(gameData, cardId);
        } else if (cast.sourceZone() == Zone.OUTSIDE_GAME) {
            gameData.outsideGamePlayPermissions.remove(cardId);
            gameData.outsideGameAdditionalModalModePermissions.remove(cardId);
        } else if (cast.sourceZone() == Zone.EXILE) {
            gameData.clearDelayedActions(ReturnExiledCardToHandAtNextEndStep.class,
                    action -> action.cardId().equals(cardId));
            clearExileCastCostChanges(gameData, cardId);
            if (gameData.removeFromExile(cardId) && (cast.exileCast() == null || !cast.exileCast().copy())) {
                gameData.recordCardPlayedFromExile(cast.controllerId());
            }
        }
    }

    /**
     * Puts the card back where it was taken from. A card cast from exile never left it, but a copy
     * that isn't cast ceases to exist (CR 707.10a) and a card whose cast says so goes to its owner's
     * hand. A revealed card held outside every zone joins the cards that weren't cast: its owner's
     * graveyard, or the bottom of their library. A card Word of Command plays stays in its hand.
     */
    private void returnToSourceZone(GameData gameData, SplitCardCast cast) {
        if (cast.origin() instanceof SplitCardCast.WordOfCommandCast) {
            // The card never left its hand; Word of Command's play simply ends.
            gameData.wordOfCommandCastingCard = false;
            return;
        }
        SplitCardCast.RevealedCast revealedCast = cast.revealedCast();
        if (revealedCast != null) {
            if (revealedCast.cardsToBottom() == null) {
                graveyardService.addCardToGraveyard(gameData, cast.ownerId(), cast.physicalCard(), Zone.LIBRARY);
            } else {
                List<Card> uncast = new ArrayList<>(revealedCast.cardsToBottom());
                uncast.add(cast.physicalCard());
                putOnLibraryBottom(gameData, cast.ownerId(), uncast);
            }
            return;
        }
        if (cast.sourceZone() == Zone.EXILE) {
            UUID cardId = cast.physicalCard().getId();
            gameData.exilePlayCostModifiers.remove(cardId);
            SplitCardCast.ExileCast exileCast = cast.exileCast();
            if (exileCast != null && exileCast.copy()) {
                clearExileCastCostChanges(gameData, cardId);
                gameData.removeFromExile(cardId);
            } else if (exileCast != null && exileCast.putIntoHandIfNotCast()) {
                ExiledCardEntry exiledEntry = gameData.findExiledCard(cardId);
                if (exiledEntry != null && gameData.removeFromExile(cardId)) {
                    clearExileCastCostChanges(gameData, cardId);
                    gameData.addCardToHand(exiledEntry.ownerId(), exiledEntry.card());
                    gameLogService.append(gameData, GameLog.cardThen(exiledEntry.card(),
                            " isn't cast and is put into its owner's hand."));
                }
            }
            return;
        }
        List<Card> zone = switch (cast.sourceZone()) {
            case HAND -> gameData.playerHands.computeIfAbsent(cast.ownerId(), ignored -> new ArrayList<>());
            case GRAVEYARD -> gameData.playerGraveyards.computeIfAbsent(cast.ownerId(), ignored -> new ArrayList<>());
            case LIBRARY -> gameData.playerDecks.computeIfAbsent(cast.ownerId(), ignored -> new ArrayList<>());
            case OUTSIDE_GAME -> OutsideGameCards.view(gameData, cast.ownerId());
            default -> throw new IllegalStateException("Can't cast a split card from " + cast.sourceZone());
        };
        zone.add(Math.min(cast.sourceIndex(), zone.size()), cast.physicalCard());
    }

    /** Puts held cards on the bottom of their owner's library, in an order the owner chooses. */
    private void putOnLibraryBottom(GameData gameData, UUID ownerId, List<Card> cards) {
        if (cards.isEmpty()) {
            return;
        }
        if (cards.size() == 1) {
            gameData.playerDecks.computeIfAbsent(ownerId, ignored -> new ArrayList<>()).add(cards.getFirst());
            return;
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(ownerId,
                new ArrayList<>(cards), true, ownerId,
                "Put these cards on the bottom of your library in any order (first chosen will be closest to the top)."));
    }

    /** Drops the cost changes installed for an exiled card once it leaves exile. */
    private static void clearExileCastCostChanges(GameData gameData, UUID cardId) {
        gameData.exilePlayCostModifiers.remove(cardId);
        gameData.exilePlayWithoutPayingManaCost.remove(cardId);
    }

    private static int modeEncoding(ChooseOneEffect modal, int modeIndex) {
        return ChooseOneEffect.encodeModeSelection(modal, modeIndex);
    }

    private static boolean needsTargets(Card card, List<CardEffect> spellEffects) {
        return EffectResolution.needsSpellCastTarget(spellEffects,
                card.isAuraThatRequiresAttachment(), card.isEnchantPlayer())
                || EffectResolution.needsSpellTarget(spellEffects);
    }

    /** Whether the half declares its own target slots, rather than a single cast-time target. */
    private static boolean hasTargetSlots(Card card) {
        return !card.getTargetGroups().isEmpty();
    }

    private static ChooseOneEffect splitModal(Card card) {
        return card.getSpellEffects().stream()
                .filter(ChooseOneEffect.class::isInstance)
                .map(ChooseOneEffect.class::cast)
                .findFirst()
                .orElse(null);
    }
}
