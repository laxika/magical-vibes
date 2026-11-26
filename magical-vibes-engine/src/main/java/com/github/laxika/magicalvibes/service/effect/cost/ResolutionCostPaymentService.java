package com.github.laxika.magicalvibes.service.effect.cost;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.AllOfCost;
import com.github.laxika.magicalvibes.model.effect.AnyOfCost;
import com.github.laxika.magicalvibes.model.effect.ExileCardsCost;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.UnlessAnswer;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DepartPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SourceControllerDrawsCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.effect.normalfx.StepDepartureSupport;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.ability.cost.MultiplePermanentTapCostHandler;
import com.github.laxika.magicalvibes.service.ability.cost.TapCostSupport;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceCost;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.DamageFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DiscardHandCost;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GetPoisonCountersCost;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeForColorlessManaUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayTapLandsYouDontControlForSpellsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.effect.WaterbendPaymentService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.DiscardHandEffectHandler;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PlayerInteractionSupport;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Pays a cost a resolving spell or ability asks a player for: "[do something]" in an "if you do / unless" construction
 * is a cost paid as the effect resolves (CR 118.12, 118.12a), by its controller or by any other player the text names.
 * A caller asks {@link #couldPossiblyPay} whether to ask the payer at all (no question only when they have no way at
 * all to pay), or {@link #canPay} (CR 118.3: a cost can't be paid without the resources to pay it fully) and, once the
 * payer chose to pay, {@link #pay}. Each cost type is paid through the action every other payment of it uses, so the
 * events and replacements of that action apply:
 * <ul>
 *   <li>{@link PayManaCost}: from the payer's pool, judged against the mana they could still make (they may activate
 *   mana abilities while asked, CR 605.3a), {B} paid with 2 life under K'rrik, Son of Yawgmoth when the mana falls
 *   short; mana paid for a permanent or spell another player controls is a tax paid to them
 *   ({@link ActionEvents#taxPaid}, the event the cast and activation taxes raise). Its {@code manaCost} must be the
 *   cost as it is paid now (no reduction, increase or source cost left to read); {@code announcedX} lets mana spendable
 *   only on {X} pay it, and {@code lifeAmount} is paid with it.</li>
 *   <li>{@link PayLifeCost} with a fixed amount: losing that life ({@link LifeSupport#applyLifePayment}, CR 119.4), only
 *   while the payer can lose life (CR 119.8, {@link #canPayLife}).</li>
 *   <li>{@link DiscardCardTypeCost} and {@link DiscardRandomCardCost}: the shared discard prompt or random discard, so
 *   madness and "whenever a player discards" see it; caused by the source's controller's ability when the payer is
 *   another player (CR 701.9).</li>
 *   <li>{@link SacrificePermanentCost}: the shared sacrifice choice, through the sacrifice action (sacrifice triggers,
 *   death replacements).</li>
 *   <li>{@link SacrificeMultiplePermanentsCost}: exactly that many chosen permanents, sacrificed together with their
 *   death replacements asked first (CR 616.1).</li>
 *   <li>{@link DiscardHandCost}: the discard-hand action; payable with an empty hand.</li>
 *   <li>{@link PutCounterOnControlledCreatureCost#blight} with a fixed count: the payer puts the -1/-1 counters on one
 *   creature they control that can get them (CR 701.68a, 701.68b), through the counter action and its replacements,
 *   on the resolving entry.</li>
 *   <li>{@link CollectEvidenceCost} (plain "collect evidence N"): the graveyard choice with its minimum mana value and
 *   the evidence-collected event (CR 701.59a, 701.59b).</li>
 *   <li>{@link ExileGraveyardCost}: every card of the payer's graveyard exiled, its exile events included; payable with
 *   an empty graveyard.</li>
 *   <li>{@link GetPoisonCountersCost}: the poison-counter action, payable only while the payer can get poison
 *   counters.</li>
 *   <li>{@link DamageFromSourceCost} with a fixed amount: the resolving entry deals the damage (CR 608.2h), through the
 *   damage event and its preventions; paid whether or not it is prevented (CR 118.12).</li>
 *   <li>{@link WaterbendCost} with a fixed amount: the waterbend payment, the payer choosing which artifacts and
 *   creatures to tap (CR 701.67a); the mana part is a tax like any other mana.</li>
 *   <li>{@link SacrificeSelfCost}: the resolving entry's source permanent, while it is still that object (CR 400.7)
 *   under the payer's control, through the sacrifice action with its death replacements asked first (CR 616.1).</li>
 *   <li>{@link ExileNCardsFromGraveyardCost}: exactly that many matching cards of the payer's graveyard, chosen one at
 *   a time when more are there, unasked when only that many are; the exile events and the {@code EXILE} step.</li>
 *   <li>{@link TapMultiplePermanentsCost} with a fixed count: untapped matching permanents that can become tapped, the
 *   source left out when the cost says so (CR 118.3), picked one at a time, all of them unasked when only that many
 *   are there.</li>
 *   <li>{@link ExileCardsCost}: the cards its subject names where they are expected to be, exiled and recorded as the
 *   entry's {@code EXILE} step; a card that left can't be exiled, so the cost can't be paid.</li>
 *   <li>{@link AnyOfCost}: payable while any alternative is ({@link #canPay}); its caller asks and pays one
 *   alternative at a time.</li>
 *   <li>{@link AllOfCost}: payable while every part is; its caller checks {@link #canPayNow} and pays one part at a
 *   time (CR 601.2h).</li>
 *   <li>{@link SourceControllerDrawsCost}: the source's controller draws (the draw action, its replacements); always
 *   payable.</li>
 * </ul>
 * A sacrifice is offered only the permanents the payer could sacrifice for that effect (CR 608.2d, 701.21a): none while
 * an effect of an opponent can't make them sacrifice (Sigarda, Host of Herons: "you can't take that option"), never one
 * that can't be sacrificed or chosen, nor a creature token the asking triggered ability can't make its controller
 * sacrifice (The Master, Multiplied).
 *
 * <p>A ransom paid together after everyone answered ({@code UnlessPaysEffect.payingTogether()}, CR 101.4) is chosen
 * first ({@link #choose}: the permanents picked, {@link #completePick}) and paid once the last player answered
 * ({@link #payTogether}: one sacrifice, tap, damage or draw step for every choice).
 * A payment with a choice ({@link Outcome#ASKING}) is finished by that prompt's completion, which ends through its own
 * epilogue and resumes the asking resolution ({@link #completePermanentChoice}, {@link #completeMultiPermanentChoice},
 * the evidence and discard completions); a life payment that a Lich's Duel Mastery sacrifice would follow asks that
 * sacrifice's death replacements first (CR 616.1) and completes the answer again once chosen.
 */
@Component
public class ResolutionCostPaymentService {

    /** How a {@link #pay} call ended. */
    public enum Outcome {
        /** The cost is paid in full. */
        PAID,
        /** The payer couldn't pay it in full; nothing was paid (CR 118.3). */
        NOT_PAID,
        /** A prompt (the card to discard, the permanent to sacrifice, a death replacement) finishes the payment. */
        ASKING
    }

    /**
     * Who and what a payment is for.
     *
     * @param sourceCard      the card whose effect asks for the payment, for log lines and prompts
     * @param sourceControllerId the controller of that effect: mana another player pays is a tax paid to them, and a
     *                        discard they make another player make is caused by an opponent
     * @param answering       the answer this payment completes, asked again after a death replacement question, or
     *                        {@code null} when the payment isn't an answer to a "may" prompt
     * @param entry           the resolving entry that asks for the payment: the dealer of a damage cost (CR 608.2h)
     *                        and the entry a blight's counters are put through, or {@code null}
     */
    public record Payment(Card sourceCard, UUID sourceControllerId, PendingMayAbility answering, StackEntry entry) {

        public Payment(Card sourceCard, UUID sourceControllerId, PendingMayAbility answering) {
            this(sourceCard, sourceControllerId, answering, null);
        }
    }

    /**
     * How a {@link #choose} call ended: {@link Outcome#PAID} when the choice is made ({@code objectIds} the permanents
     * chosen, none for a payment with nothing to pick), {@link Outcome#ASKING} while a pick finishes it
     * ({@link #completePick}), {@link Outcome#NOT_PAID} when the payer couldn't pay (CR 118.3).
     */
    public record Choice(Outcome outcome, List<UUID> objectIds) {

        public Choice {
            objectIds = List.copyOf(objectIds);
        }

        static Choice chosen(List<UUID> objectIds) {
            return new Choice(Outcome.PAID, objectIds);
        }
    }

    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;
    @Autowired
    @Lazy
    private DrawService drawService;
    @Autowired
    @Lazy
    private PotentialManaService potentialManaService;
    @Autowired
    @Lazy
    private PermanentChoiceHandlerService permanentChoiceHandlerService;
    @Autowired
    @Lazy
    private PredicateEvaluationService predicateEvaluationService;
    @Autowired
    @Lazy
    private PlayerInputService playerInputService;
    @Autowired
    @Lazy
    private PlayerInteractionSupport playerInteractionSupport;
    @Autowired
    @Lazy
    private WaterbendPaymentService waterbendPaymentService;
    @Autowired
    @Lazy
    private PermanentCounterSupport permanentCounterSupport;
    @Autowired
    @Lazy
    private DestructionSupport destructionSupport;
    @Autowired
    @Lazy
    private PermanentRemovalService permanentRemovalService;
    @Autowired
    @Lazy
    private GraveyardService graveyardService;
    @Autowired
    @Lazy
    private ExileService exileService;
    @Autowired
    @Lazy
    private DiscardHandEffectHandler discardHandEffectHandler;
    @Autowired
    @Lazy
    private DamageSupport damageSupport;
    @Autowired
    @Lazy
    private GameOutcomeService gameOutcomeService;
    @Autowired
    @Lazy
    private GameLogService gameLogService;
    @Autowired
    @Lazy
    private AbilityActivationService abilityActivationService;
    @Autowired
    @Lazy
    private TapCostSupport tapCostSupport;
    @Autowired
    @Lazy
    private GraveyardReturnSupport graveyardReturnSupport;
    @Autowired
    @Lazy
    private SubjectResolutionService subjectResolutionService;

    public ResolutionCostPaymentService(GameQueryService gameQueryService, LifeSupport lifeSupport) {
        this.gameQueryService = gameQueryService;
        this.lifeSupport = lifeSupport;
    }

    /**
     * Whether {@code payerId} could pay {@code cost}, asked for by {@code sourceCard}'s effect, in full now (CR 118.3).
     *
     * @throws IllegalArgumentException for a cost type with no resolution-time payment yet
     */
    public boolean canPay(GameData gameData, UUID payerId, CostEffect cost, Card sourceCard) {
        return canPay(gameData, payerId, cost, sourceCard, null);
    }

    /**
     * {@link #canPay(GameData, UUID, CostEffect, Card)} for the payment {@code entry} asks for as it resolves: the
     * costs about its source ({@link SacrificeSelfCost}, a tap leaving the source out), its cards
     * ({@link ExileCardsCost}) or a filter judged from its source need it.
     */
    public boolean canPay(GameData gameData, UUID payerId, CostEffect cost, Card sourceCard, StackEntry entry) {
        return switch (cost) {
            case PayManaCost mana -> lifeForMana(gameData, payerId, mana, true) >= 0;
            case PayLifeCost life -> canPayLife(gameData, payerId, fixedLife(life));
            case DiscardCardTypeCost discard ->
                    discardableIndices(gameData, payerId, discard, sourceCard).size() >= discard.count();
            case DiscardRandomCardCost discard -> handSize(gameData, payerId) >= discard.count();
            case SacrificePermanentCost sacrifice ->
                    !sacrificeCandidates(gameData, payerId, sacrifice.filter(), sacrifice.excludeSource(), entry)
                            .isEmpty();
            case SacrificeMultiplePermanentsCost sacrifice ->
                    sacrificeCandidates(gameData, payerId, sacrifice.filter(), false, entry).size()
                            >= sacrifice.count();
            case DiscardHandCost ignored -> true;
            case PutCounterOnControlledCreatureCost ignored -> !blightCandidates(gameData, payerId).isEmpty();
            case CollectEvidenceCost evidence -> graveyardManaValue(gameData, payerId) >= evidence.minimumManaValue();
            case ExileGraveyardCost ignored -> true;
            case GetPoisonCountersCost ignored -> gameQueryService.canPlayerGetPoisonCounters(gameData, payerId);
            case DamageFromSourceCost ignored -> true;
            case WaterbendCost waterbend -> waterbendPaymentService.canPay(gameData, payerId, waterbend.amount());
            case SacrificeSelfCost ignored -> sourceToSacrifice(gameData, payerId, entry) != null;
            case ExileNCardsFromGraveyardCost exile ->
                    graveyardExileCandidates(gameData, payerId, exile, entry).size() >= exile.count();
            case TapMultiplePermanentsCost tap -> tapHandler(gameData, tap, entry).getValidChoiceIds(gameData, payerId)
                    .size() >= tapCount(gameData, tap, entry);
            case ExileCardsCost exile -> !cardsToExile(gameData, entry, exile).isEmpty();
            case SourceControllerDrawsCost ignored -> true;
            case AnyOfCost anyOf -> anyOf.alternatives().stream()
                    .anyMatch(alternative -> canPay(gameData, payerId, alternative, sourceCard, entry));
            case AllOfCost allOf -> allOf.parts().stream()
                    .allMatch(part -> canPay(gameData, payerId, part, sourceCard, entry));
            default -> throw new IllegalArgumentException("No resolution-time payment for " + cost);
        };
    }

    /**
     * Whether {@code payerId} could pay {@code cost} in full from what they have this moment, their mana pool only:
     * an all-of cost is paid only when every part is (CR 601.2h, 118.3), and no part is paid before that is known.
     */
    public boolean canPayNow(GameData gameData, UUID payerId, CostEffect cost, Card sourceCard, StackEntry entry) {
        return switch (cost) {
            case PayManaCost mana -> lifeForMana(gameData, payerId, mana, false) >= 0;
            case AllOfCost allOf -> allOf.parts().stream()
                    .allMatch(part -> canPayNow(gameData, payerId, part, sourceCard, entry));
            default -> canPay(gameData, payerId, cost, sourceCard, entry);
        };
    }

    /**
     * Whether {@code payerId} could possibly pay {@code cost} as it is asked for now, so is asked at all: {@code false}
     * only when it is certain they can't, as they have no way at all to pay it (CR 118.3, 608.2d). Where it can't be
     * sure, it answers {@code true} and the payer is asked; {@link #pay} still pays only what they then have.
     * <ul>
     *   <li>Life alone ({@link PayLifeCost}): {@link #canPayLife} (CR 119.4, 119.8). No mana is asked for, so no mana
     *   ability can be activated meanwhile to change the life total (CR 605.3a).</li>
     *   <li>Mana ({@link PayManaCost}, a {@link WaterbendCost}'s mana part): their pool pays it ({B} as 2 life under
     *   K'rrik, Son of Yawgmoth included), or they have any mana source at all ({@link #hasManaSource}), whatever it
     *   could make: with no activated mana ability, no triggered one can trigger either (CR 605.1b).</li>
     *   <li>Mana and life: a payer who can't lose life can't pay it (CR 119.8); otherwise the life falls short for
     *   certain only when no mana ability could be activated (a mana ability may gain life), so the mana decides.</li>
     *   <li>{@link AllOfCost}: every part. A part other than mana that can't be paid now proves it only when the
     *   payer has no mana source at all or the cost asks for no mana: activating a mana ability while asked may
     *   change the hand, graveyard or battlefield (Lotus Petal goes to the graveyard, Chromatic Sphere draws).</li>
     *   <li>Every other cost: {@link #canPay}, which no mana payment can change.</li>
     *   <li>{@link AnyOfCost}: any alternative.</li>
     * </ul>
     */
    public boolean couldPossiblyPay(GameData gameData, UUID payerId, CostEffect cost, Card sourceCard) {
        return couldPossiblyPay(gameData, payerId, cost, sourceCard, null);
    }

    /** {@link #couldPossiblyPay(GameData, UUID, CostEffect, Card)} for the payment {@code entry} asks for. */
    public boolean couldPossiblyPay(GameData gameData, UUID payerId, CostEffect cost, Card sourceCard,
                                    StackEntry entry) {
        return switch (cost) {
            case PayLifeCost life -> canPayLife(gameData, payerId, fixedLife(life));
            case PayManaCost mana -> couldPossiblyPayMana(gameData, payerId, mana);
            case WaterbendCost waterbend -> canPay(gameData, payerId, waterbend, sourceCard)
                    || hasManaSource(gameData, payerId);
            case AnyOfCost anyOf -> anyOf.alternatives().stream()
                    .anyMatch(alternative -> couldPossiblyPay(gameData, payerId, alternative, sourceCard, entry));
            case AllOfCost allOf -> {
                PayManaCost mana = allOf.manaPart();
                if (mana != null && !couldPossiblyPayMana(gameData, payerId, mana)) {
                    yield false;
                }
                boolean manaAbilitiesMayChangeTheRest = mana != null && hasManaSource(gameData, payerId);
                yield allOf.parts().stream().filter(part -> part != mana).allMatch(part ->
                        manaAbilitiesMayChangeTheRest || canPay(gameData, payerId, part, sourceCard, entry));
            }
            default -> canPay(gameData, payerId, cost, sourceCard, entry);
        };
    }

    private boolean couldPossiblyPayMana(GameData gameData, UUID payerId, PayManaCost mana) {
        if (mana.lifeAmount() > 0 && !gameQueryService.canPlayerLoseLife(gameData, payerId)) {
            return false;
        }
        if (lifeForMana(gameData, payerId, mana, false) >= 0) {
            return true;
        }
        return manaCostOf(mana) != null && hasManaSource(gameData, payerId);
    }

    /**
     * Whether {@code playerId} has any way at all to make mana: a permanent they control with a mana ability (an
     * intrinsic basic land type's, a printed or granted one, tapped or not, whatever its cost); another player's
     * permanent's mana ability any player may activate; a card they own in their hand, graveyard, exile or the command
     * zone with a mana ability that works there (Elvish Spirit Guide); or a permission to make mana (Channel's "pay 1
     * life: add {C}", tapping lands they don't control for mana). Counted generously: an answer of {@code false} is
     * a proof that no mana can be made.
     */
    public boolean hasManaSource(GameData gameData, UUID playerId) {
        if (abilityActivationService == null) {
            return false;
        }
        if (gameData.hasFloatingEffectOnPlayer(playerId, MayPayLifeForColorlessManaUntilEndOfTurnEffect.class)
                || gameData.hasFloatingEffectOnPlayer(playerId,
                MayTapLandsYouDontControlForSpellsUntilEndOfTurnEffect.class)) {
            return true;
        }
        for (Map.Entry<UUID, List<Permanent>> battlefield : gameData.playerBattlefields.entrySet()) {
            boolean own = battlefield.getKey().equals(playerId);
            for (Permanent permanent : battlefield.getValue()) {
                if (own && (!gameQueryService.intrinsicBasicLandManaColors(gameData, permanent).isEmpty()
                        || !gameQueryService.getOverriddenLandManaColors(gameData, permanent).isEmpty())) {
                    return true;
                }
                for (ActivatedAbility ability : abilityActivationService.getEffectiveActivatedAbilities(
                        gameData, permanent)) {
                    if ((own || ability.isActivatableByAnyPlayer()) && AbilityActivationService.isManaAbility(ability)) {
                        return true;
                    }
                }
            }
        }
        for (Card card : gameData.playerHands.getOrDefault(playerId, List.of())) {
            if (hasManaAbility(abilityActivationService.effectiveHandAbilities(gameData, card, playerId))) {
                return true;
            }
        }
        for (Card card : gameData.playerGraveyards.getOrDefault(playerId, List.of())) {
            if (hasManaAbility(card.getActivatedAbilities(Zone.GRAVEYARD))) {
                return true;
            }
        }
        synchronized (gameData.exiledCards) {
            for (ExiledCardEntry exiled : gameData.exiledCards) {
                if (playerId.equals(exiled.ownerId()) && hasManaAbility(exiled.card().getActivatedAbilities(Zone.EXILE))) {
                    return true;
                }
            }
        }
        for (Card card : gameData.playerCommandZones.getOrDefault(playerId, List.of())) {
            if (hasManaAbility(card.getActivatedAbilities(Zone.COMMAND))) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasManaAbility(List<ActivatedAbility> abilities) {
        return abilities.stream().anyMatch(AbilityActivationService::isManaAbility);
    }

    /**
     * Pays {@code cost} for {@code payer}, who chose to pay it, from what they have now; a cost they can't pay in full
     * isn't paid at all ({@link Outcome#NOT_PAID}, CR 118.3).
     */
    public Outcome pay(GameData gameData, Player payer, CostEffect cost, Payment payment) {
        UUID payerId = payer.getId();
        return switch (cost) {
            case PayManaCost mana -> payMana(gameData, payer, mana, payment);
            case PayLifeCost life -> payLife(gameData, payer, fixedLife(life), payment);
            case DiscardCardTypeCost discard -> {
                List<Integer> indices = discardableIndices(gameData, payerId, discard, payment.sourceCard());
                if (indices.size() < discard.count()) {
                    yield Outcome.NOT_PAID;
                }
                gameData.discardCausedByOpponent = causedByOpponent(payerId, payment);
                playerInputService.beginDiscardChoice(gameData, payerId, indices,
                        discard.count() == 1 ? "Choose a card to discard." : "Choose " + discard.count()
                                + " cards to discard.", discard.count());
                yield Outcome.ASKING;
            }
            case DiscardRandomCardCost discard -> {
                if (handSize(gameData, payerId) < discard.count()) {
                    yield Outcome.NOT_PAID;
                }
                gameData.discardCausedByOpponent = causedByOpponent(payerId, payment);
                playerInteractionSupport.resolveRandomDiscardCards(gameData, payerId,
                        payment.sourceCard().getName(), discard.count());
                yield Outcome.PAID;
            }
            case SacrificePermanentCost sacrifice -> {
                List<UUID> candidates = sacrificeCandidates(gameData, payerId, sacrifice.filter(),
                        sacrifice.excludeSource(), payment.entry());
                if (candidates.isEmpty()) {
                    yield Outcome.NOT_PAID;
                }
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.SacrificePermanentThen(payerId, payment.sourceCard(), null));
                playerInputService.beginPermanentChoice(gameData, payerId, candidates,
                        "Choose " + (sacrifice.description() == null ? "a permanent" : "a " + sacrifice.description())
                                + " to sacrifice.");
                yield Outcome.ASKING;
            }
            case SacrificeMultiplePermanentsCost sacrifice -> {
                List<UUID> candidates = sacrificeCandidates(gameData, payerId, sacrifice.filter(), false,
                        payment.entry());
                if (candidates.size() < sacrifice.count()) {
                    yield Outcome.NOT_PAID;
                }
                playerInputService.beginMultiPermanentChoice(gameData, payerId, candidates, sacrifice.count(),
                        new MultiPermanentChoiceContext.ResolutionCostPayment(sacrifice, payerId, payment.sourceCard(),
                                payment.sourceControllerId(), sacrifice.count()),
                        "Choose " + sacrifice.count() + " "
                                + (sacrifice.description() == null ? "permanents" : sacrifice.description())
                                + " to sacrifice.");
                yield Outcome.ASKING;
            }
            case DiscardHandCost ignored -> {
                discardHand(gameData, payerId, payment.sourceCard());
                yield afterImmediatePayment(gameData);
            }
            case PutCounterOnControlledCreatureCost blight -> {
                List<UUID> candidates = blightCandidates(gameData, payerId);
                if (candidates.isEmpty()) {
                    yield Outcome.NOT_PAID;
                }
                gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ResolutionCostPayment(
                        blight, payerId, payment.sourceCard(), payment.sourceControllerId()));
                playerInputService.beginPermanentChoice(gameData, payerId, candidates, "Choose a creature to blight.");
                yield Outcome.ASKING;
            }
            case CollectEvidenceCost evidence -> {
                List<Card> graveyard = gameData.playerGraveyards.get(payerId);
                if (graveyardManaValue(gameData, payerId) < evidence.minimumManaValue()) {
                    yield Outcome.NOT_PAID;
                }
                gameData.graveyardTargetOperation.resolutionTimeCollectEvidenceResume = true;
                gameData.graveyardTargetOperation.collectEvidenceAsResolutionCost = true;
                playerInputService.beginMultiGraveyardChoiceWithMinimumManaValue(gameData, payerId,
                        new ArrayList<>(graveyard), graveyard.size(), evidence.minimumManaValue(),
                        "Choose cards from your graveyard to collect evidence " + evidence.minimumManaValue() + ".");
                yield Outcome.ASKING;
            }
            case ExileGraveyardCost ignored -> {
                List<Card> graveyard = gameData.playerGraveyards.get(payerId);
                if (graveyard != null && !graveyard.isEmpty()) {
                    List<Card> exiled = new ArrayList<>(graveyard);
                    graveyard.clear();
                    graveyardService.notifyCardsExiledFromGraveyard(gameData, payerId, exiled);
                    for (Card card : exiled) {
                        exileService.exileCard(gameData, payerId, card);
                    }
                }
                yield afterImmediatePayment(gameData);
            }
            case GetPoisonCountersCost poison -> {
                if (!gameQueryService.canPlayerGetPoisonCounters(gameData, payerId)) {
                    yield Outcome.NOT_PAID;
                }
                lifeSupport.applyPoisonCounters(gameData, payerId, poison.count(), payment.sourceCard().getName(),
                        payment.sourceControllerId());
                yield afterImmediatePayment(gameData);
            }
            case DamageFromSourceCost damage -> {
                int amount = damage.amount() instanceof Fixed fixed ? fixed.value() : -1;
                if (amount < 0 || payment.entry() == null) {
                    throw new IllegalArgumentException("Read the damage and name its dealer before paying: " + damage);
                }
                // The object whose effect asks deals it, as it exists or last existed (CR 608.2h).
                if (!damageSupport.isDamageSourcePreventedWithLog(gameData, payment.entry())) {
                    damageSupport.dealDamageToPlayer(gameData, payment.entry(), payerId, amount);
                }
                gameOutcomeService.checkWinCondition(gameData);
                yield afterImmediatePayment(gameData);
            }
            case WaterbendCost waterbend -> {
                int amount = waterbend.amount();
                if (!waterbendPaymentService.canPay(gameData, payerId, amount)) {
                    yield Outcome.NOT_PAID;
                }
                List<UUID> choosable = waterbendPaymentService.untappedArtifactsAndCreatures(gameData, payerId);
                int minimumTaps = waterbendPaymentService.minimumTaps(gameData, payerId, amount);
                if (choosable.size() > minimumTaps) {
                    // The payer decides which permanents to tap (CR 701.67a); the cost is paid once they choose.
                    playerInputService.beginMultiPermanentChoice(gameData, payerId, choosable,
                            Math.min(amount, choosable.size()),
                            new MultiPermanentChoiceContext.ResolutionCostPayment(waterbend, payerId,
                                    payment.sourceCard(), payment.sourceControllerId(), minimumTaps),
                            payment.sourceCard().getName() + " — Choose artifacts and creatures to tap for waterbend {"
                                    + amount + "}; the rest is paid with mana.");
                    yield Outcome.ASKING;
                }
                payWaterbend(gameData, payerId, waterbend, payment.sourceCard(), payment.sourceControllerId(), List.of());
                yield afterImmediatePayment(gameData);
            }
            case SacrificeSelfCost ignored -> {
                Permanent source = sourceToSacrifice(gameData, payerId, payment.entry());
                if (source == null) {
                    yield Outcome.NOT_PAID;
                }
                destructionSupport.askDeathReplacementsBeforeSacrifice(gameData, List.of(source));
                destructionSupport.sacrificeAndLog(gameData, source, payerId);
                permanentRemovalService.removeOrphanedAuras(gameData);
                yield afterImmediatePayment(gameData);
            }
            case ExileNCardsFromGraveyardCost exile -> {
                List<Card> candidates = graveyardExileCandidates(gameData, payerId, exile, payment.entry());
                if (candidates.size() < exile.count()) {
                    yield Outcome.NOT_PAID;
                }
                if (candidates.size() > exile.count()) {
                    // The payer picks which cards (one at a time); the choice's completion resumes the resolution.
                    graveyardReturnSupport.beginGraveyardExileChoice(gameData, payerId, candidates, exile.count(),
                            null);
                    yield Outcome.ASKING;
                }
                List<Card> graveyard = gameData.playerGraveyards.get(payerId);
                graveyard.removeAll(candidates);
                graveyardService.notifyCardsExiledFromGraveyard(gameData, payerId, candidates);
                gameData.runAsSingleCardExileBatch(() -> candidates.forEach(card ->
                        exileService.exileCard(gameData, payerId, card)));
                if (payment.entry() != null) {
                    payment.entry().recordStep(StepKind.EXILE, StepOutcome.ofObjects(
                            candidates.stream().map(Card::getId).toList(), List.of(payerId)));
                }
                yield afterImmediatePayment(gameData);
            }
            case TapMultiplePermanentsCost tap -> {
                MultiplePermanentTapCostHandler handler = tapHandler(gameData, tap, payment.entry());
                int required = tapCount(gameData, tap, payment.entry());
                if (handler.getValidChoiceIds(gameData, payerId).size() < required) {
                    yield Outcome.NOT_PAID;
                }
                yield tapOrAsk(gameData, payer, tap, handler, required, payment.sourceCard(),
                        payment.sourceControllerId(), payment.entry() == null ? null
                                : payment.entry().getSourcePermanentId());
            }
            case ExileCardsCost exile -> {
                List<SubjectResolutionService.LocatedCard> cards = cardsToExile(gameData, payment.entry(), exile);
                if (cards.isEmpty()) {
                    yield Outcome.NOT_PAID;
                }
                StackEntry entry = payment.entry();
                entry.recordStep(StepKind.EXILE, StepOutcome.NONE);
                for (SubjectResolutionService.LocatedCard located : cards) {
                    Card card = located.card();
                    permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, card.getId());
                    exileService.exileCard(gameData, located.ownerId(), card);
                    entry.addToStep(StepKind.EXILE, StepOutcome.ofObjects(List.of(card.getId()),
                            List.of(located.ownerId())));
                    gameLogService.append(gameData, GameLog.textCardText(payer.getUsername() + " exiles ", card,
                            "."));
                }
                yield afterImmediatePayment(gameData);
            }
            case SourceControllerDrawsCost draw -> {
                if (payment.sourceControllerId() != null) {
                    drawService.resolveDrawCards(gameData, payment.sourceControllerId(), draw.count());
                }
                yield afterImmediatePayment(gameData);
            }
            case AllOfCost ignored -> throw new IllegalArgumentException(
                    "An all-of cost is paid one part at a time, after canPayNow: " + cost);
            default -> throw new IllegalArgumentException("No resolution-time payment for " + cost);
        };
    }

    /**
     * {@code payer}'s choice to pay {@code cost}, paid later with the other players' ({@link #payTogether}): nothing
     * changes now. The permanents of a sacrifice or tap are picked now, unasked when only that many could be chosen
     * (CR 608.2d); a pick asked ({@link PermanentChoiceContext.AnyPlayerPaymentPick},
     * {@link MultiPermanentChoiceContext.AnyPlayerPaymentPick}) is kept on the resolving entry by {@link #completePick}.
     * Damage and a draw have nothing to choose.
     */
    public Choice choose(GameData gameData, Player payer, CostEffect cost, Payment payment) {
        UUID payerId = payer.getId();
        StackEntry entry = payment.entry();
        return switch (cost) {
            case SacrificePermanentCost sacrifice -> pick(gameData, payerId,
                    sacrificeCandidates(gameData, payerId, sacrifice.filter(), sacrifice.excludeSource(), entry), 1,
                    "Choose " + (sacrifice.description() == null ? "a permanent" : "a " + sacrifice.description())
                            + " to sacrifice.");
            case SacrificeMultiplePermanentsCost sacrifice -> pick(gameData, payerId,
                    sacrificeCandidates(gameData, payerId, sacrifice.filter(), false, entry), sacrifice.count(),
                    "Choose " + sacrifice.count() + " "
                            + (sacrifice.description() == null ? "permanents" : sacrifice.description())
                            + " to sacrifice.");
            case TapMultiplePermanentsCost tap -> {
                int count = tapCount(gameData, tap, entry);
                List<UUID> candidates = tapHandler(gameData, tap, entry).getValidChoiceIds(gameData, payerId).stream()
                        .filter(id -> !gameQueryService.cantBeChosen(gameData,
                                gameQueryService.findPermanentById(gameData, id)))
                        .toList();
                yield pick(gameData, payerId, candidates, count, count == 1 ? "Choose a permanent to tap." : "Choose " + count + " permanents to tap.");
            }
            case DamageFromSourceCost ignored -> Choice.chosen(List.of());
            case SourceControllerDrawsCost ignored -> Choice.chosen(List.of());
            default -> throw new IllegalArgumentException("No payment chosen now and paid later for " + cost);
        };
    }

    private Choice pick(GameData gameData, UUID payerId, List<UUID> candidates, int count, String prompt) {
        if (candidates.size() < count) {
            return new Choice(Outcome.NOT_PAID, List.of());
        }
        if (candidates.size() == count) {
            return Choice.chosen(candidates);
        }
        if (count == 1) {
            playerInputService.beginPermanentChoice(gameData, payerId, candidates,
                    new PermanentChoiceContext.AnyPlayerPaymentPick(payerId), prompt);
        } else {
            playerInputService.beginMultiPermanentChoice(gameData, payerId, candidates, count,
                    new MultiPermanentChoiceContext.AnyPlayerPaymentPick(payerId), prompt);
        }
        return new Choice(Outcome.ASKING, List.of());
    }

    /**
     * Completes an {@code AnyPlayerPaymentPick}: {@code payerId}'s picked permanents are kept with their choice on the
     * waiting entry's {@link UnlessAnswer}, to be paid with the others once everyone answered. The caller ends through
     * the epilogue, which resumes the asking resolution.
     */
    public void completePick(GameData gameData, UUID payerId, List<UUID> permanentIds) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            return;
        }
        UnlessAnswer state = entry.unlessAnswer(gameData.pendingEffectResolutionIndex);
        if (state != null) {
            entry.setUnlessAnswer(state.withChoice(payerId, permanentIds));
        }
    }

    /**
     * The steps paying {@code cost} for every one of {@code choices} together, after the last answer (CR 101.4,
     * 608.2e): the chosen permanents sacrificed as one departure (death replacements asked first, CR 616.1; they die
     * together, CR 603.10a) or tapped as one tap step; the source dealing the damage to every payer at once, dealt by
     * the object as it is or last was (CR 608.2h) and paid even if prevented (CR 118.12); the source's controller
     * drawing once for the round, however many chose it (Development's rulings). A chosen permanent that is gone or no
     * longer its chooser's, or a payer who left the game, is skipped: nothing else is done instead (CR 609.3), and the
     * branch still follows the choice (CR 118.12).
     */
    public List<CardEffect> payTogether(GameData gameData, StackEntry entry, CostEffect cost,
                                        List<UnlessAnswer.Choice> choices) {
        List<UnlessAnswer.Choice> live = choices.stream()
                .filter(choice -> gameData.playerIds.contains(choice.payerId()))
                .toList();
        List<UUID> payers = live.stream().map(UnlessAnswer.Choice::payerId).distinct().toList();
        if (payers.isEmpty()) {
            return List.of();
        }
        List<UUID> chosen = new ArrayList<>();
        for (UnlessAnswer.Choice choice : live) {
            for (UUID id : choice.objectIds()) {
                Permanent permanent = gameQueryService.findPermanentById(gameData, id);
                if (permanent != null
                        && choice.payerId().equals(gameQueryService.findPermanentController(gameData, id))) {
                    chosen.add(id);
                }
            }
        }
        return switch (cost) {
            case SacrificePermanentCost ignored -> sacrificeTogether(entry, payers, chosen);
            case SacrificeMultiplePermanentsCost ignored -> sacrificeTogether(entry, payers, chosen);
            case TapMultiplePermanentsCost ignored -> {
                entry.recordStep(StepKind.CHOOSE, StepOutcome.ofObjects(chosen, payers));
                yield List.of(new TapPermanentsEffect(Subjects.thisWay(StepKind.CHOOSE)));
            }
            case DamageFromSourceCost damage -> {
                entry.setAcceptedPlayerIds(payers);
                yield List.of(new DealDamageToPlayersEffect(damage.amount(), Subjects.acceptedPlayers()));
            }
            case SourceControllerDrawsCost draw -> List.of(new DrawCardEffect(draw.count(), Subjects.you()));
            default -> throw new IllegalArgumentException("No payment made together for " + cost);
        };
    }

    private static List<CardEffect> sacrificeTogether(StackEntry entry, List<UUID> payers, List<UUID> chosen) {
        entry.setAcceptedPlayerIds(payers);
        return chosen.isEmpty() ? List.of()
                : List.of(new DepartPermanentsEffect(StepDepartureSupport.sacrifices(chosen)));
    }

    /**
     * Taps {@code required} of the permanents {@code handler} offers: all of them unasked when only that many can be
     * tapped (CR 608.2d), else one pick at a time ({@link PermanentChoiceContext.ResolutionCostPayment}).
     */
    private Outcome tapOrAsk(GameData gameData, Player payer, TapMultiplePermanentsCost tap,
                             MultiplePermanentTapCostHandler handler, int required, Card sourceCard,
                             UUID sourceControllerId, UUID sourcePermanentId) {
        UUID payerId = payer.getId();
        if (handler.shouldAutoPayAll(gameData, payerId, required)) {
            for (UUID id : handler.getValidChoiceIds(gameData, payerId)) {
                Permanent chosen = gameQueryService.findPermanentById(gameData, id);
                if (chosen != null) {
                    handler.validateAndPay(gameData, payer, chosen);
                }
            }
            return afterImmediatePayment(gameData);
        }
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ResolutionCostPayment(
                tap, payerId, sourceCard, sourceControllerId, sourcePermanentId, required));
        playerInputService.beginPermanentChoice(gameData, payerId, handler.getValidChoiceIds(gameData, payerId),
                handler.getPromptMessage(required));
        return Outcome.ASKING;
    }

    /**
     * Completes a {@link PermanentChoiceContext.ResolutionCostPayment}: {@code payerId} chose {@code permanentId} to
     * pay the cost (a blight's creature). The counters are put by the payer through the counter action on the resolving
     * entry; how many it ends up with doesn't matter, the cost is paid (CR 118.12). The caller ends through the
     * epilogue, which resumes the asking resolution.
     */
    public void completePermanentChoice(GameData gameData, UUID payerId, UUID permanentId,
                                        PermanentChoiceContext.ResolutionCostPayment context) {
        if (context.cost() instanceof TapMultiplePermanentsCost tap) {
            completeTapPick(gameData, payerId, permanentId, context, tap);
            return;
        }
        if (!(context.cost() instanceof PutCounterOnControlledCreatureCost blight)) {
            throw new IllegalStateException("No permanent choice pays " + context.cost());
        }
        if (!blightCandidates(gameData, payerId).contains(permanentId)) {
            throw new IllegalStateException("That creature can't be blighted");
        }
        Permanent creature = gameQueryService.findPermanentById(gameData, permanentId);
        StackEntry entry = gameData.pendingEffectResolutionEntry != null
                ? gameData.pendingEffectResolutionEntry : new StackEntry(context.sourceCard(), payerId);
        permanentCounterSupport.placeCounterOnPermanentForPlayer(gameData, entry, creature,
                CounterType.MINUS_ONE_MINUS_ONE, blight.count(), payerId);
    }

    /**
     * One pick of a tap cost (CR 118.3: an untapped permanent of the payer's that matches): it is tapped, and while
     * more are needed the next pick is asked, or the rest tapped unasked when only that many are left.
     */
    private void completeTapPick(GameData gameData, UUID payerId, UUID permanentId,
                                 PermanentChoiceContext.ResolutionCostPayment context, TapMultiplePermanentsCost tap) {
        Player payer = new Player(payerId, gameData.playerIdToName.get(payerId));
        MultiplePermanentTapCostHandler handler = new MultiplePermanentTapCostHandler(tap, context.remaining(),
                predicateEvaluationService, gameLogService, context.sourcePermanentId());
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosen == null || !handler.getValidChoiceIds(gameData, payerId).contains(permanentId)) {
            throw new IllegalStateException("That permanent can't be tapped for this cost");
        }
        handler.validateAndPay(gameData, payer, chosen);
        int remaining = context.remaining() - handler.lastPaymentWeight();
        if (remaining <= 0) {
            return;
        }
        if (!handler.canPayRemaining(gameData, payerId, remaining)) {
            throw new IllegalStateException("Not enough permanents remaining");
        }
        tapOrAsk(gameData, payer, tap, handler, remaining, context.sourceCard(), context.sourceControllerId(),
                context.sourcePermanentId());
    }

    /**
     * Whether {@code permanentIds} answer {@code context}: exactly the cost's number of the payer's matching permanents
     * to sacrifice, or between its minimum and the waterbend amount of untapped artifacts and creatures to tap.
     *
     * @throws IllegalStateException when they don't
     */
    public void validateMultiPermanentChoice(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                             MultiPermanentChoiceContext.ResolutionCostPayment context) {
        if (!playerId.equals(context.payerId())) {
            throw new IllegalStateException("Not the payer's choice");
        }
        if (permanentIds.stream().distinct().count() != permanentIds.size()) {
            throw new IllegalStateException("A permanent was chosen twice");
        }
        switch (context.cost()) {
            case SacrificeMultiplePermanentsCost sacrifice -> {
                if (permanentIds.size() != sacrifice.count()) {
                    throw new IllegalStateException("Must select exactly " + sacrifice.count() + " permanents");
                }
                List<UUID> candidates = sacrificeCandidates(gameData, playerId, sacrifice.filter(), false,
                        gameData.pendingEffectResolutionEntry);
                if (!candidates.containsAll(permanentIds)) {
                    throw new IllegalStateException("A selected permanent can't be sacrificed for this cost");
                }
            }
            case WaterbendCost waterbend -> {
                if (permanentIds.size() < context.minimum() || permanentIds.size() > waterbend.amount()) {
                    throw new IllegalStateException("Must tap at least " + context.minimum()
                            + " permanents (the rest of the cost is paid with mana)");
                }
            }
            default -> throw new IllegalStateException("No multi-permanent choice pays " + context.cost());
        }
    }

    /**
     * Completes a {@link MultiPermanentChoiceContext.ResolutionCostPayment} with the chosen {@code permanentIds}
     * (validated by {@link #validateMultiPermanentChoice}): sacrifices them together, their death replacements asked
     * first (CR 616.1), or taps them for the waterbend and pays the rest with mana. The caller ends through the
     * epilogue, which resumes the asking resolution.
     */
    public void completeMultiPermanentChoice(GameData gameData, UUID playerId, List<UUID> permanentIds,
                                             MultiPermanentChoiceContext.ResolutionCostPayment context) {
        switch (context.cost()) {
            case SacrificeMultiplePermanentsCost ignored -> {
                List<Permanent> chosen = permanentIds.stream()
                        .map(permanentId -> gameQueryService.findPermanentById(gameData, permanentId))
                        .filter(permanent -> permanent != null && playerId.equals(
                                gameQueryService.findPermanentController(gameData, permanent.getId())))
                        .toList();
                destructionSupport.askDeathReplacementsBeforeSacrifice(gameData, chosen);
                for (Permanent permanent : chosen) {
                    destructionSupport.sacrificeAndLog(gameData, permanent, playerId);
                }
                permanentRemovalService.removeOrphanedAuras(gameData);
            }
            case WaterbendCost waterbend -> payWaterbend(gameData, playerId, waterbend, context.sourceCard(),
                    context.sourceControllerId(), permanentIds);
            default -> throw new IllegalStateException("No multi-permanent choice pays " + context.cost());
        }
    }

    /**
     * Completes the evidence collected for a {@link CollectEvidenceCost} ransom: the chosen cards are exiled and the
     * evidence-collected event raised (CR 701.59a); the asking effect resumes where it waits.
     */
    public void completeCollectEvidence(GameData gameData, Player payer, List<Card> exiledCards) {
        UUID payerId = payer.getId();
        List<Card> graveyard = gameData.playerGraveyards.get(payerId);
        if (graveyard != null) {
            graveyard.removeAll(exiledCards);
            graveyardService.notifyCardsExiledFromGraveyard(gameData, payerId, exiledCards);
            for (Card card : exiledCards) {
                exileService.exileCard(gameData, payerId, card);
            }
            gameLogService.append(gameData, GameLog.text(payer.getUsername() + " exiles " + exiledCards.size()
                    + " cards from their graveyard to collect evidence."));
        }
        com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents.evidenceCollected(gameData, payerId);
    }

    /**
     * Pays a waterbend: the chosen (or, with none chosen, the fewest needed) artifacts and creatures are tapped and
     * the rest is paid with mana, which is a tax when the payment is for another player's spell or permanent.
     */
    private void payWaterbend(GameData gameData, UUID payerId, WaterbendCost waterbend, Card sourceCard,
                              UUID sourceControllerId, List<UUID> tapped) {
        ManaPool pool = gameData.playerManaPools.get(payerId);
        int before = pool == null ? 0 : pool.getTotalAllMana();
        waterbendPaymentService.pay(gameData, payerId, waterbend.amount(), sourceCard, tapped);
        int after = pool == null ? 0 : pool.getTotalAllMana();
        ActionEvents.taxPaid(gameData, payerId, sourceControllerId, before - after);
    }

    /**
     * {@code payerId} discards their hand through the discard-hand action (madness, "whenever a player discards"), as
     * their own instruction: the payer chose to pay, so the discard is theirs.
     */
    private void discardHand(GameData gameData, UUID payerId, Card sourceCard) {
        DiscardHandEffect discard = new DiscardHandEffect(Subjects.targetPlayer());
        StackEntry carrier = StackEntry.carrier(sourceCard, payerId, sourceCard.getName() + " - discard hand",
                List.of(discard), payerId, (UUID) null);
        discardHandEffectHandler.resolve(gameData, carrier, discard);
    }

    /** A payment made at once is paid, unless one of its actions stopped to ask (a replacement's choice). */
    private static Outcome afterImmediatePayment(GameData gameData) {
        return gameData.interaction.isAwaitingInput() ? Outcome.ASKING : Outcome.PAID;
    }

    /** The creatures {@code payerId} controls that can get -1/-1 counters and be chosen (CR 701.68b). */
    private List<UUID> blightCandidates(GameData gameData, UUID payerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(payerId);
        if (battlefield == null) {
            return List.of();
        }
        return battlefield.stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(permanent -> !gameQueryService.cantHaveCounters(gameData, permanent))
                .filter(permanent -> !gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, permanent))
                .filter(permanent -> !gameQueryService.cantBeChosen(gameData, permanent))
                .map(Permanent::getId)
                .toList();
    }

    private static int graveyardManaValue(GameData gameData, UUID playerId) {
        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        return graveyard == null ? 0 : graveyard.stream().mapToInt(Card::getManaValue).sum();
    }

    /**
     * Whether {@code playerId} could pay {@code amount} life: 0 always; more needs that much life and the ability to
     * lose life, as a player who can't lose life can't pay life (CR 119.4, 119.8). This is the check for every life
     * payment a resolving effect asks for; "can't pay life" prohibitions on casting and activating (Karn's Sylex) don't
     * apply to it.
     */
    public static boolean canPayLife(GameQueryService gameQueryService, GameData gameData, UUID playerId, int amount) {
        return amount <= 0 || gameQueryService.canPlayerLoseLife(gameData, playerId)
                && gameData.getLife(playerId) >= amount;
    }

    /** {@link #canPayLife(GameQueryService, GameData, UUID, int)} with this service's query service. */
    public boolean canPayLife(GameData gameData, UUID playerId, int amount) {
        return canPayLife(gameQueryService, gameData, playerId, amount);
    }

    /** Whether {@code payerId} could pay {@code cost} from their pool or with the mana they could still make. */
    public boolean canPayMana(GameData gameData, UUID payerId, ManaCost cost) {
        return cost.canPayResolutionCost(gameData.playerManaPools.get(payerId))
                || potentialManaService != null
                && cost.canPay(potentialManaService.buildVirtualManaPool(gameData, payerId));
    }

    /**
     * Pays {@code cost} from {@code payerId}'s pool, raising the tax-paid event for the mana spent when the payment is
     * for {@code taxingPlayerId}'s spell or permanent; {@code false}, with nothing paid, when the pool can't pay it.
     */
    public boolean payManaFromPool(GameData gameData, UUID payerId, ManaCost cost, UUID taxingPlayerId) {
        ManaPool pool = gameData.playerManaPools.get(payerId);
        if (pool == null || !cost.canPayResolutionCost(pool)) {
            return false;
        }
        int before = pool.getTotalAllMana();
        cost.payResolutionCost(pool);
        ActionEvents.taxPaid(gameData, payerId, taxingPlayerId, before - pool.getTotalAllMana());
        return true;
    }

    /** {@code mana}'s cost as a {@link ManaCost}, or {@code null} for a life-only payment. */
    public static ManaCost manaCostOf(PayManaCost mana) {
        if (mana.genericReduction() != null || mana.genericIncrease() != null || mana.useSourceManaCost()) {
            throw new IllegalArgumentException("Read the mana cost before paying it: " + mana);
        }
        if (mana.manaCost() == null || mana.manaCost().isEmpty()) {
            return null;
        }
        ManaCost cost = new ManaCost(mana.manaCost(), mana.forCumulativeUpkeep());
        return mana.announcedX() ? cost.asAnnouncedXCost() : cost;
    }

    /**
     * The life {@code payerId} pays with {@code mana} (its life part plus 2 for each {B} paid with life under K'rrik), or
     * -1 when they can't pay it in full; {@code potential} counts the mana they could still make (the question), else
     * only their pool (the answer).
     */
    private int lifeForMana(GameData gameData, UUID payerId, PayManaCost mana, boolean potential) {
        ManaCost cost = manaCostOf(mana);
        int lifeForBlack = 0;
        if (cost != null && !canPayManaNow(gameData, payerId, cost, potential)) {
            ManaCost blackAsLife = cost.withBlackManaAsPhyrexian();
            if (blackAsLife == cost || !gameQueryService.canPayBlackManaWithLife(gameData, payerId)
                    || !canPayManaNow(gameData, payerId, blackAsLife, potential)) {
                return -1;
            }
            lifeForBlack = blackAsLife.payPhyrexianManaAuto(new ManaPool(gameData.playerManaPools.get(payerId)), 0);
        }
        int life = mana.lifeAmount() + lifeForBlack;
        return canPayLife(gameData, payerId, life) ? life : -1;
    }

    private boolean canPayManaNow(GameData gameData, UUID payerId, ManaCost cost, boolean potential) {
        return potential ? canPayMana(gameData, payerId, cost)
                : cost.canPayResolutionCost(gameData.playerManaPools.get(payerId));
    }

    private Outcome payMana(GameData gameData, Player payer, PayManaCost mana, Payment payment) {
        UUID payerId = payer.getId();
        int life = lifeForMana(gameData, payerId, mana, false);
        if (life < 0) {
            return Outcome.NOT_PAID;
        }
        ManaCost cost = manaCostOf(mana);
        ManaPool pool = gameData.playerManaPools.get(payerId);
        boolean phyrexian = cost != null && !cost.canPayResolutionCost(pool);
        Runnable payManaThenLife = () -> {
            if (cost != null) {
                ManaCost toPay = phyrexian ? cost.withBlackManaAsPhyrexian() : cost;
                int before = pool.getTotalAllMana();
                if (phyrexian) {
                    toPay.payPhyrexianManaAuto(pool, 0);
                }
                toPay.payResolutionCost(pool);
                ActionEvents.taxPaid(gameData, payerId, payment.sourceControllerId(), before - pool.getTotalAllMana());
            }
            if (life > 0) {
                lifeSupport.applyLifePayment(gameData, payerId, life, payment.sourceCard().getName());
            }
        };
        return payAskingDeathReplacementsFirst(gameData, payer, life, payment, payManaThenLife);
    }

    private Outcome payLife(GameData gameData, Player payer, int life, Payment payment) {
        UUID payerId = payer.getId();
        if (!canPayLife(gameData, payerId, life)) {
            return Outcome.NOT_PAID;
        }
        return payAskingDeathReplacementsFirst(gameData, payer, life, payment,
                () -> lifeSupport.applyLifePayment(gameData, payerId, life, payment.sourceCard().getName()));
    }

    /**
     * Runs {@code payment}; paying life is losing it (CR 119.4), so when the payment answers a prompt, a Lich's Duel
     * Mastery sacrifice it would cause has its death replacements asked first (CR 616.1), and the answer is completed
     * again once they are chosen ({@link Outcome#ASKING}).
     */
    private Outcome payAskingDeathReplacementsFirst(GameData gameData, Player payer, int life, Payment payment,
                                                    Runnable pay) {
        if (life <= 0 || payment.answering() == null || permanentChoiceHandlerService == null) {
            pay.run();
            return Outcome.PAID;
        }
        boolean stopped = permanentChoiceHandlerService.answerAskingDeathReplacementsFirst(
                gameData, payer, payment.answering(), true, () -> {
                    lifeSupport.askBeforeLifeLossesThatFollowAChange(gameData, Map.of(payer.getId(), life));
                    pay.run();
                });
        return stopped ? Outcome.ASKING : Outcome.PAID;
    }

    private static int fixedLife(PayLifeCost life) {
        if (life.amount() instanceof Fixed fixed) {
            return fixed.value();
        }
        throw new IllegalArgumentException("Read the life amount before paying it: " + life);
    }

    private static boolean causedByOpponent(UUID payerId, Payment payment) {
        return payment.sourceControllerId() != null && !payment.sourceControllerId().equals(payerId);
    }

    private static int handSize(GameData gameData, UUID playerId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        return hand == null ? 0 : hand.size();
    }

    private List<Integer> discardableIndices(GameData gameData, UUID payerId, DiscardCardTypeCost discard,
                                             Card sourceCard) {
        List<Card> hand = gameData.playerHands.get(payerId);
        List<Integer> indices = new ArrayList<>();
        if (hand == null) {
            return indices;
        }
        for (int i = 0; i < hand.size(); i++) {
            if (discard.predicate() == null || predicateEvaluationService.matchesCardPredicate(
                    hand.get(i), discard.predicate(), sourceCard == null ? null : sourceCard.getId(), gameData, payerId)) {
                indices.add(i);
            }
        }
        return indices;
    }

    /**
     * The payer's permanents they could sacrifice for {@code filter}, judged from the resolving {@code entry}'s source
     * ("an Aura attached to Three Dog") and without that source when the cost leaves it out: none while the entry's
     * controller, an opponent, can't make them sacrifice (Sigarda, Host of Herons' rulings); never one that can't be
     * sacrificed or chosen, nor a creature token the asking triggered ability can't make its controller sacrifice
     * (CR 608.2d, 701.21a).
     */
    private List<UUID> sacrificeCandidates(GameData gameData, UUID payerId,
                                           com.github.laxika.magicalvibes.model.filter.PermanentPredicate filter,
                                           boolean excludeSource, StackEntry entry) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(payerId);
        if (battlefield == null || entry != null
                && !gameQueryService.canEffectCauseSacrifice(gameData, payerId, entry.getControllerId())) {
            return List.of();
        }
        FilterContext context = filterContext(gameData, entry);
        UUID sourceId = entry == null ? null : entry.getSourcePermanentId();
        return battlefield.stream()
                .filter(permanent -> !excludeSource || sourceId == null || !permanent.getId().equals(sourceId))
                .filter(permanent -> filter == null || (context == null
                        ? predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, filter)
                        : predicateEvaluationService.matchesPermanentPredicate(permanent, filter, context)))
                .filter(permanent -> !gameQueryService.cantBeChosenToSacrifice(gameData, permanent, filter))
                .filter(permanent -> !gameQueryService.isCreatureTokenProtectedFromTriggeredAbility(gameData,
                        permanent, entry))
                .map(Permanent::getId)
                .toList();
    }

    /**
     * The filter context of a resolving entry's source, or {@code null} without an entry; it names the card of the
     * permanent the entry targets, so "another" than that one ({@code PermanentIsTargetCardPredicate}) can be judged.
     */
    private FilterContext filterContext(GameData gameData, StackEntry entry) {
        if (entry == null) {
            return null;
        }
        Permanent target = entry.getTargetId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getTargetId());
        return FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() == null ? null : entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withTargetCardId(target == null ? null : target.getOriginalCard().getId());
    }

    /**
     * The resolving entry's source permanent while it is still that object on the battlefield (CR 400.7) under
     * {@code payerId}'s control, which "sacrifice this" needs; else {@code null}.
     */
    private Permanent sourceToSacrifice(GameData gameData, UUID payerId, StackEntry entry) {
        if (entry == null || entry.getSourcePermanentId() == null) {
            return null;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        return source != null && payerId.equals(gameQueryService.findPermanentController(gameData, source.getId()))
                ? source : null;
    }

    /** The cards of {@code payerId}'s graveyard that may be exiled for {@code exile}. */
    private List<Card> graveyardExileCandidates(GameData gameData, UUID payerId, ExileNCardsFromGraveyardCost exile,
                                                StackEntry entry) {
        List<Card> graveyard = gameData.playerGraveyards.get(payerId);
        if (graveyard == null) {
            return List.of();
        }
        UUID sourceCardId = entry == null || entry.getCard() == null ? null : entry.getCard().getId();
        return graveyard.stream()
                .filter(card -> exile.requiredType() == null || card.hasType(exile.requiredType()))
                .filter(card -> exile.predicate() == null || predicateEvaluationService.matchesCardPredicate(
                        card, exile.predicate(), sourceCardId, gameData, payerId))
                .toList();
    }

    /** The tap handler of {@code tap} for the resolving entry's source (left out when the cost says so). */
    private MultiplePermanentTapCostHandler tapHandler(GameData gameData, TapMultiplePermanentsCost tap,
                                                       StackEntry entry) {
        return new MultiplePermanentTapCostHandler(tap, tapCount(gameData, tap, entry), predicateEvaluationService,
                gameLogService, entry == null ? null : entry.getSourcePermanentId());
    }

    private int tapCount(GameData gameData, TapMultiplePermanentsCost tap, StackEntry entry) {
        if (tap.count() instanceof Fixed fixed) {
            return fixed.value();
        }
        return tapCostSupport.requiredCount(gameData, tap, entry == null ? null : entry.getSourcePermanentId(),
                entry == null ? 0 : entry.getXValue());
    }

    /** The cards {@code exile} names for {@code entry} where they are expected to be (CR 400.7). */
    private List<SubjectResolutionService.LocatedCard> cardsToExile(GameData gameData, StackEntry entry,
                                                                    ExileCardsCost exile) {
        if (entry == null || subjectResolutionService == null) {
            return List.of();
        }
        return subjectResolutionService.locatedCards(gameData, entry, null, exile.cards()).stream()
                .filter(located -> located.zone() == Zone.GRAVEYARD)
                .toList();
    }
}
