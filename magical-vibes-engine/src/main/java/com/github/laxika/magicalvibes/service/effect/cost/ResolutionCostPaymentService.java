package com.github.laxika.magicalvibes.service.effect.cost;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.effect.DrawCardsCost;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.FlipCoinsCost;
import com.github.laxika.magicalvibes.model.effect.GainControlOfPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.OpponentCreatesTokensCost;
import com.github.laxika.magicalvibes.model.effect.OpponentGainsLifeCost;
import com.github.laxika.magicalvibes.model.effect.PutCardsFromSingleGraveyardOnBottomOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnOpponentCreatureCost;
import com.github.laxika.magicalvibes.service.effect.normalfx.CoinFlipCostSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.CoinFlipService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LibraryExileSupport;
import com.github.laxika.magicalvibes.model.effect.MillControllerCost;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.PutCardsFromGraveyardOnBottomOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.PutTypedCounterOnSourceCost;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardToHandCost;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardTopExileSupport;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
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
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
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
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandCost;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GetPoisonCountersCost;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.PayXManaCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.LandDropCost;
import com.github.laxika.magicalvibes.model.effect.PutCardFromHandOnTopOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentToHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreaturesWithTotalPowerCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
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
 *   <li>{@link PayXManaCost}: the X chosen by its caller first (CR 107.3f), then {@link #paidWith} that X paid as
 *   {@link PayManaCost}; {@link #couldPossiblyPay} asks whether X = 1 might be paid within its limit, and
 *   {@link #mostXPayable} bounds the question.</li>
 *   <li>{@link PayLifeCost} with a fixed amount: losing that life ({@link LifeSupport#applyLifePayment}, CR 119.4), only
 *   while the payer can lose life (CR 119.8, {@link #canPayLife}), recorded as the entry's {@code PAY_LIFE} step.</li>
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
 *   <li>{@link ReturnPermanentToHandCost}: a matching permanent the payer controls (any player's when the cost says
 *   so), judged by its current characteristics from the entry's source ("another"), chosen and returned to its
 *   owner's hand.</li>
 *   <li>{@link PutCardFromHandOnTopOfLibraryCost}: a card the payer chooses from their hand, put on top of their
 *   library; payable while their hand isn't empty.</li>
 *   <li>{@link LandDropCost} ({D}): one of the payer's land drops this turn, payable only on their own turn while
 *   they have a land drop left.</li>
 *   <li>{@link SacrificeCreaturesWithTotalPowerCost}: any number of the payer's creatures they could sacrifice, whose
 *   total power reaches the amount, chosen together and sacrificed together with their death replacements asked
 *   first (CR 616.1).</li>
 *   <li>The cumulative upkeep costs (CR 702.24a: each choice made once for each age counter, all or none paid):
 *   {@link FlipCoinsCost} (the coin-flip action, its win and loss events, inside the Krark's Thumb window of the
 *   answer); {@link OpponentCreatesTokensCost} (the opponent creates the tokens through the token action);
 *   {@link PutCounterOnOpponentCreatureCost} (one creature an opponent controls chosen for each counter, the same one
 *   again allowed, through the counter action); {@link DrawCardsCost} (the draw action, payable unless the payer
 *   can't draw; a replaced draw still pays it, Psychic Vortex's ruling); {@link ExileTopCardOfLibraryCost} (while
 *   the library has that many cards); {@link PutCardsFromSingleGraveyardOnBottomOfLibraryCost} (each payment's cards
 *   chosen from one graveyard, Jötun Grunt's ruling); {@link OpponentGainsLifeCost} (the life-gain action, payable
 *   while the opponent can gain life); {@link GainControlOfPermanentsCost} (a different matching permanent the payer
 *   doesn't control for each counter, Herald of Leshrac's ruling, chosen one at a time).</li>
 * </ul>
 * A sacrifice is offered only the permanents the payer could sacrifice for that effect (CR 608.2d, 701.21a): none while
 * an effect of an opponent can't make them sacrifice (Sigarda, Host of Herons: "you can't take that option"), never one
 * that can't be sacrificed or chosen, nor a creature token the asking triggered ability can't make its controller
 * sacrifice (The Master, Multiplied).
 *
 * <p>A ransom paid together after everyone answered ({@code UnlessPaysEffect.payingTogether()}, CR 101.4) is chosen
 * first ({@link #choose}: the permanents picked, {@link #completePick}) and paid once the last player answered
 * ({@link #payTogether}: one sacrifice, tap, damage, draw or discard step for every choice).
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
    @Autowired
    @Lazy
    private InteractionHandlerRegistry interactionHandlerRegistry;
    @Autowired
    @Lazy
    private GraveyardTopExileSupport graveyardTopExileSupport;
    @Autowired
    @Lazy
    private CoinFlipCostSupport coinFlipCostSupport;
    @Autowired
    @Lazy
    private CoinFlipService coinFlipService;
    @Autowired
    @Lazy
    private LibraryExileSupport libraryExileSupport;

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
            case PayXManaCost xCost -> maximumX(xCost) >= 1
                    && lifeForMana(gameData, payerId, paidWith(xCost, 1), true) >= 0;
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
            case ReturnPermanentToHandCost toHand ->
                    returnCandidates(gameData, payerId, toHand, entry).size() >= toHand.count();
            case PutCardFromHandOnTopOfLibraryCost ignored -> handSize(gameData, payerId) >= 1;
            case PayEnergyCost energy -> gameData.playerEnergyCounters.getOrDefault(payerId, 0)
                    >= energy.fixedAmount();
            case ExileTopCardOfGraveyardCost top ->
                    graveyardTopExileSupport.findTopMatching(gameData, payerId, top.requiredType()) != null;
            case RemoveCounterFromSourceCost counters -> sourceWithCounters(gameData, entry, counters) != null;
            case RemoveCounterFromControlledPermanentCost counters ->
                    removableCounters(gameData, payerId, counters, entry) >= counters.count();
            case PutCardsFromGraveyardOnBottomOfLibraryCost bottom ->
                    gameData.playerGraveyards.getOrDefault(payerId, List.of()).size() >= bottom.count();
            case ReturnCardFromGraveyardToHandCost toHand ->
                    !graveyardReturnIndices(gameData, payerId, toHand, entry).isEmpty();
            case MillControllerCost mill -> gameData.playerDecks.getOrDefault(payerId, List.of()).size()
                    >= mill.count();
            case PutTypedCounterOnSourceCost counter -> sourceForCounter(gameData, entry, counter) != null;
            case LandDropCost ignored -> hasLandDropLeft(gameData, payerId);
            case SacrificeCreaturesWithTotalPowerCost power ->
                    reachablePower(gameData, powerCandidates(gameData, payerId, entry)) >= power.totalPower();
            case FlipCoinsCost ignored -> true;
            case OpponentCreatesTokensCost ignored -> gameQueryService.getOpponentId(gameData, payerId) != null;
            case PutCounterOnOpponentCreatureCost counters ->
                    !opponentCreaturesForCounter(gameData, payerId, counters.counterType()).isEmpty();
            case DrawCardsCost ignored -> !drawService.isDrawPrevented(gameData, payerId);
            case ExileTopCardOfLibraryCost exile -> libraryExileSupport.hasAtLeast(gameData, payerId, exile.count());
            case PutCardsFromSingleGraveyardOnBottomOfLibraryCost bottom -> singleGraveyardPayments(gameData, bottom)
                    >= bottom.payments();
            case OpponentGainsLifeCost ignored -> {
                UUID opponentId = gameQueryService.getOpponentId(gameData, payerId);
                yield opponentId != null && gameQueryService.canPlayerGainLife(gameData, opponentId);
            }
            case GainControlOfPermanentsCost control ->
                    destructionSupport.collectPermanentIdsNotControlledBy(gameData, payerId, control.filter()).size()
                            >= control.count();
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
            case PayXManaCost xCost -> maximumX(xCost) >= 1 && couldPossiblyPayMana(gameData, payerId,
                    paidWith(xCost, 1));
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
            case PayXManaCost ignored -> throw new IllegalArgumentException(
                    "The X is chosen first, then the cost with it is paid (paidWith): " + cost);
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
                        "Choose " + (sacrifice.description() == null ? "a permanent" : withArticle(sacrifice.description()))
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
                // The payer performs the cost's action, so they put the counters (CR 118.12a).
                lifeSupport.applyPoisonCounters(gameData, payerId, poison.count(), payment.sourceCard().getName(),
                        payerId);
                yield afterImmediatePayment(gameData);
            }
            case DamageFromSourceCost damage -> {
                int amount = damage.amount() instanceof Fixed fixed ? fixed.value() : -1;
                if (amount < 0 || payment.entry() == null) {
                    throw new IllegalArgumentException("Read the damage and name its dealer before paying: " + damage);
                }
                // The object whose effect asks deals it, as it exists or last existed (CR 608.2h).
                damageSupport.dealDamageToPlayer(gameData, payment.entry(), payerId, amount);
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
            case ReturnPermanentToHandCost toHand when toHand.count() > 1 -> {
                List<UUID> candidates = returnCandidates(gameData, payerId, toHand, payment.entry());
                if (candidates.size() < toHand.count()) {
                    yield Outcome.NOT_PAID;
                }
                playerInputService.beginMultiPermanentChoice(gameData, payerId, candidates, toHand.count(),
                        new MultiPermanentChoiceContext.ResolutionCostPayment(toHand, payerId, payment.sourceCard(),
                                payment.sourceControllerId(), toHand.count()),
                        "Choose " + toHand.count() + " "
                                + (toHand.description() == null ? "permanents you control" : toHand.description())
                                + " to return to their owner's hand.");
                yield Outcome.ASKING;
            }
            case PayEnergyCost energy -> {
                int amount = energy.fixedAmount();
                int has = gameData.playerEnergyCounters.getOrDefault(payerId, 0);
                if (has < amount) {
                    yield Outcome.NOT_PAID;
                }
                gameData.setPlayerEnergyCounters(payerId, has - amount);
                yield Outcome.PAID;
            }
            case ExileTopCardOfGraveyardCost top ->
                    graveyardTopExileSupport.exileTopMatching(gameData, payerId, top.requiredType())
                            ? afterImmediatePayment(gameData) : Outcome.NOT_PAID;
            case RemoveCounterFromSourceCost counters -> {
                Permanent source = sourceWithCounters(gameData, payment.entry(), counters);
                if (source == null) {
                    yield Outcome.NOT_PAID;
                }
                permanentCounterSupport.removeCounterFromPermanent(gameData, source, counters.counterType(),
                        counters.count());
                yield afterImmediatePayment(gameData);
            }
            case RemoveCounterFromControlledPermanentCost counters -> {
                if (removableCounters(gameData, payerId, counters, payment.entry()) < counters.count()) {
                    yield Outcome.NOT_PAID;
                }
                yield removeNextCounters(gameData, payerId, counters,
                        payment.entry() == null ? null : payment.entry().getSourcePermanentId(), counters.count(), true);
            }
            case PutCardsFromGraveyardOnBottomOfLibraryCost bottom -> {
                List<Card> graveyard = gameData.playerGraveyards.getOrDefault(payerId, List.of());
                if (graveyard.size() < bottom.count()) {
                    yield Outcome.NOT_PAID;
                }
                if (bottom.count() == 1) {
                    interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                            .builder(payerId, java.util.stream.IntStream.range(0, graveyard.size()).boxed().toList(),
                                    GraveyardChoiceDestination.BOTTOM_OF_OWNERS_LIBRARY,
                                    "Choose a card to put on the bottom of your library.")
                            .mandatory(true)
                            .build());
                    yield Outcome.ASKING;
                }
                gameData.graveyardTargetOperation.resolutionCostLibraryBottom =
                        new GraveyardTargetOperationState.ResolutionCostLibraryBottomContext(payerId, bottom.count());
                playerInputService.beginMultiGraveyardChoice(gameData, payerId, new ArrayList<>(graveyard),
                        bottom.count(), bottom.count(), "Choose " + bottom.count()
                                + " cards from your graveyard to put on the bottom of your library.");
                yield Outcome.ASKING;
            }
            case ReturnCardFromGraveyardToHandCost toHand -> {
                List<Integer> indices = graveyardReturnIndices(gameData, payerId, toHand, payment.entry());
                if (indices.isEmpty()) {
                    yield Outcome.NOT_PAID;
                }
                interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                        .builder(payerId, indices, GraveyardChoiceDestination.HAND,
                                "Choose " + withArticle(CardPredicateUtils.describeFilter(toHand.predicate()))
                                        + " to return to your hand.")
                        .mandatory(true)
                        .build());
                yield Outcome.ASKING;
            }
            case MillControllerCost mill -> {
                if (gameData.playerDecks.getOrDefault(payerId, List.of()).size() < mill.count()) {
                    yield Outcome.NOT_PAID;
                }
                graveyardService.resolveMillPlayer(gameData, payerId, mill.count());
                yield afterImmediatePayment(gameData);
            }
            case PutTypedCounterOnSourceCost counter -> {
                Permanent source = sourceForCounter(gameData, payment.entry(), counter);
                if (source == null) {
                    yield Outcome.NOT_PAID;
                }
                permanentCounterSupport.placeCounterOnPermanentForPlayer(gameData, payment.entry(), source,
                        counter.counterType(), counter.count(), payerId);
                yield afterImmediatePayment(gameData);
            }
            case ReturnPermanentToHandCost toHand -> {
                List<UUID> candidates = returnCandidates(gameData, payerId, toHand, payment.entry());
                if (candidates.isEmpty()) {
                    yield Outcome.NOT_PAID;
                }
                gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ResolutionCostPayment(
                        toHand, payerId, payment.sourceCard(), payment.sourceControllerId()));
                playerInputService.beginPermanentChoice(gameData, payerId, candidates, "Choose "
                        + (toHand.description() == null ? "a permanent you control" : "a " + toHand.description())
                        + " to return to its owner's hand.");
                yield Outcome.ASKING;
            }
            case PutCardFromHandOnTopOfLibraryCost ignored -> {
                List<Card> hand = gameData.playerHands.get(payerId);
                if (hand == null || hand.isEmpty()) {
                    yield Outcome.NOT_PAID;
                }
                List<Card> snapshot = List.copyOf(hand);
                interactionHandlerRegistry.begin(gameData, PendingInteraction.PutCardsFromHandOnLibraryCardChoice
                        .putOnLibrary(payerId, snapshot.stream().map(Card::getId).toList(), snapshot, 1,
                                HandToLibraryPlacement.TOP));
                yield Outcome.ASKING;
            }
            case LandDropCost ignored -> {
                if (!hasLandDropLeft(gameData, payerId)) {
                    yield Outcome.NOT_PAID;
                }
                gameData.landsPlayedThisTurn.merge(payerId, 1, Integer::sum);
                yield Outcome.PAID;
            }
            case SacrificeCreaturesWithTotalPowerCost power -> {
                List<UUID> candidates = powerCandidates(gameData, payerId, payment.entry());
                if (reachablePower(gameData, candidates) < power.totalPower()) {
                    yield Outcome.NOT_PAID;
                }
                playerInputService.beginMultiPermanentChoice(gameData, payerId, candidates, candidates.size(),
                        new MultiPermanentChoiceContext.ResolutionCostPayment(power, payerId, payment.sourceCard(),
                                payment.sourceControllerId(), 1),
                        "Choose creatures with total power " + power.totalPower() + " or greater to sacrifice.");
                yield Outcome.ASKING;
            }
            case FlipCoinsCost flip -> flipCoins(gameData, payer, flip, payment);
            case OpponentCreatesTokensCost tokens -> {
                UUID opponentId = gameQueryService.getOpponentId(gameData, payerId);
                if (opponentId == null) {
                    yield Outcome.NOT_PAID;
                }
                for (int i = 0; i < tokens.count(); i++) {
                    destructionSupport.createTokenForPlayer(gameData, opponentId, tokens.tokenTemplate(),
                            payment.sourceCard().getName(), payment.sourceCard().getSetCode());
                }
                yield afterImmediatePayment(gameData);
            }
            case PutCounterOnOpponentCreatureCost counters -> {
                List<UUID> candidates = opponentCreaturesForCounter(gameData, payerId, counters.counterType());
                if (candidates.isEmpty()) {
                    yield Outcome.NOT_PAID;
                }
                if (candidates.size() == 1) {
                    // One creature to choose for every counter (CR 608.2d): all of them go on it.
                    StackEntry placement = payment.entry() == null
                            ? new StackEntry(payment.sourceCard(), payerId) : new StackEntry(payment.entry());
                    placement.setControllerId(payerId);
                    permanentCounterSupport.placeCounterOnPermanent(gameData, placement,
                            gameQueryService.findPermanentById(gameData, candidates.getFirst()),
                            counters.counterType(), counters.count());
                    yield afterImmediatePayment(gameData);
                }
                playerInputService.beginMultiPermanentChoice(gameData, payerId, candidates, 1,
                        new MultiPermanentChoiceContext.OpponentCreatureCounterPlacement(counters.counterType(),
                                counters.count(), payerId),
                        "Choose a creature to put a counter on.");
                yield Outcome.ASKING;
            }
            case DrawCardsCost draw -> {
                if (drawService.isDrawPrevented(gameData, payerId)) {
                    yield Outcome.NOT_PAID;
                }
                for (int i = 0; i < draw.count(); i++) {
                    drawService.resolveDrawCard(gameData, payerId);
                }
                yield afterImmediatePayment(gameData);
            }
            case ExileTopCardOfLibraryCost exile -> {
                if (!libraryExileSupport.hasAtLeast(gameData, payerId, exile.count())) {
                    yield Outcome.NOT_PAID;
                }
                libraryExileSupport.exileTopCards(gameData, payerId, exile.count());
                yield afterImmediatePayment(gameData);
            }
            case PutCardsFromSingleGraveyardOnBottomOfLibraryCost bottom -> {
                if (singleGraveyardPayments(gameData, bottom) < bottom.payments()) {
                    yield Outcome.NOT_PAID;
                }
                gameData.graveyardTargetOperation.cumulativeUpkeepPayment =
                        new GraveyardTargetOperationState.CumulativeUpkeepPaymentContext(payerId,
                                bottom.cardsPerPayment(), bottom.payments(), List.of());
                gameData.graveyardTargetOperation.singleGraveyard = true;
                playerInputService.beginMultiGraveyardChoice(gameData, payerId, graveyardCardsExcept(gameData, List.of()),
                        bottom.cardsPerPayment(), bottom.cardsPerPayment(), "Choose " + bottom.cardsPerPayment()
                                + " cards from a single graveyard for the first cumulative upkeep payment.");
                yield Outcome.ASKING;
            }
            case OpponentGainsLifeCost life -> {
                UUID opponentId = gameQueryService.getOpponentId(gameData, payerId);
                if (opponentId == null || !gameQueryService.canPlayerGainLife(gameData, opponentId)) {
                    yield Outcome.NOT_PAID;
                }
                lifeSupport.applyGainLife(gameData, opponentId, life.amount(), null, payment.sourceCard(),
                        payment.entry() == null ? null : payment.entry().getEntryType());
                yield afterImmediatePayment(gameData);
            }
            case GainControlOfPermanentsCost control -> {
                if (destructionSupport.collectPermanentIdsNotControlledBy(gameData, payerId, control.filter()).size()
                        < control.count()) {
                    yield Outcome.NOT_PAID;
                }
                yield gainControlOrAsk(gameData, payerId, control, control.count(), payment.sourceCard(),
                        payment.sourceControllerId());
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
     * Damage and a draw have nothing to choose; a discard's cards, hidden in a hand (CR 101.4a), are picked as it is
     * paid together, every payer in APNAP order, which tells the later payers only what the answers already did
     * (CR 101.4b).
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
            // The cards are picked after the last answer, hidden (CR 101.4a): the choice now is to discard.
            case DiscardCardTypeCost discard -> discardableIndices(gameData, payerId, discard, payment.sourceCard())
                    .size() >= discard.count() ? Choice.chosen(List.of()) : new Choice(Outcome.NOT_PAID, List.of());
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
     * drawing once for the round, however many chose it (Development's rulings); every payer who chose a discard picking
     * the cards in APNAP order and all of them discarded at once (the discard action, CR 101.4; Professor Onyx's
     * ruling). A chosen permanent that is gone or no
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
                yield List.of(DealDamageEffect.to(Subjects.acceptedPlayers(), damage.amount()));
            }
            case SourceControllerDrawsCost draw -> List.of(new DrawCardEffect(draw.count(), Subjects.you()));
            case DiscardCardTypeCost discard -> {
                entry.setAcceptedPlayerIds(payers);
                yield List.of(discard.predicate() == null
                        ? new DiscardEffect(discard.count(), Subjects.acceptedPlayers(), false)
                        : DiscardEffect.matching(discard.count(), Subjects.acceptedPlayers(), discard.predicate()));
            }
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
        if (context.cost() instanceof ReturnPermanentToHandCost toHand) {
            completeReturnToHand(gameData, payerId, permanentId, toHand);
            return;
        }
        if (context.cost() instanceof GainControlOfPermanentsCost control) {
            completeGainControlPick(gameData, payerId, permanentId, context, control);
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
     * Flips the coins of a {@link FlipCoinsCost}: every coin first, then the results and their win and loss events
     * ({@link CoinFlipCostSupport#pay}). Flipping is the first thing the answer changes, so a Krark's Thumb choice on a
     * flip stops it ({@link Outcome#ASKING}) and the answer is completed again once the kept results are chosen.
     */
    private Outcome flipCoins(GameData gameData, Player payer, FlipCoinsCost flip, Payment payment) {
        Runnable pay = () -> coinFlipCostSupport.pay(gameData, payer.getId(), payment.sourceCard(), flip.count());
        if (payment.answering() == null) {
            pay.run();
            return afterImmediatePayment(gameData);
        }
        boolean stopped = coinFlipService.answerChoosingResultsFirst(gameData, pay,
                new GameData.MayAnswerReplay(payer, payment.answering(), true));
        return stopped ? Outcome.ASKING : afterImmediatePayment(gameData);
    }

    /** The creatures {@code payerId}'s opponent controls that can get a counter of {@code kind}. */
    private List<UUID> opponentCreaturesForCounter(GameData gameData, UUID payerId, CounterType kind) {
        UUID opponentId = gameQueryService.getOpponentId(gameData, payerId);
        if (opponentId == null) {
            return List.of();
        }
        return gameData.playerBattlefields.getOrDefault(opponentId, List.of()).stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .filter(permanent -> !gameQueryService.cantHaveCounters(gameData, permanent))
                .filter(permanent -> kind != CounterType.MINUS_ONE_MINUS_ONE
                        || !gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, permanent))
                .filter(permanent -> kind != CounterType.PLUS_ONE_PLUS_ONE
                        || !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, permanent))
                .map(Permanent::getId)
                .toList();
    }

    /** How many payments of {@code bottom}'s size the graveyards hold, each from one graveyard. */
    private static int singleGraveyardPayments(GameData gameData, PutCardsFromSingleGraveyardOnBottomOfLibraryCost bottom) {
        int payments = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            payments += gameData.playerGraveyards.getOrDefault(playerId, List.of()).size() / bottom.cardsPerPayment();
        }
        return payments;
    }

    /** Every graveyard card but {@code chosen}, in turn order. */
    public static List<Card> graveyardCardsExcept(GameData gameData, List<UUID> chosen) {
        List<Card> cards = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            gameData.playerGraveyards.getOrDefault(playerId, List.of()).stream()
                    .filter(card -> !chosen.contains(card.getId()))
                    .forEach(cards::add);
        }
        return cards;
    }

    /**
     * Gains control of the next of {@code remaining} permanents for {@code control}: all of them unasked when only that
     * many are left to choose (CR 608.2d), else the payer picks the next one
     * ({@link PermanentChoiceContext.ResolutionCostPayment}).
     */
    private Outcome gainControlOrAsk(GameData gameData, UUID payerId, GainControlOfPermanentsCost control,
                                     int remaining, Card sourceCard, UUID sourceControllerId) {
        List<UUID> candidates = destructionSupport.collectPermanentIdsNotControlledBy(gameData, payerId,
                control.filter());
        if (candidates.size() <= remaining) {
            for (UUID id : candidates) {
                destructionSupport.gainPermanentControl(gameData, gameQueryService.findPermanentById(gameData, id),
                        payerId, sourceCard.getName());
            }
            return afterImmediatePayment(gameData);
        }
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ResolutionCostPayment(
                control, payerId, sourceCard, sourceControllerId, null, remaining));
        playerInputService.beginPermanentChoice(gameData, payerId, candidates, "Choose a permanent to gain control of"
                + (remaining > 1 ? " (" + remaining + " remaining)." : "."));
        return Outcome.ASKING;
    }

    /** One pick of a gain-control cost: the payer gains control of it, then the next pick is asked while more are due. */
    private void completeGainControlPick(GameData gameData, UUID payerId, UUID permanentId,
                                         PermanentChoiceContext.ResolutionCostPayment context,
                                         GainControlOfPermanentsCost control) {
        if (!destructionSupport.collectPermanentIdsNotControlledBy(gameData, payerId, control.filter())
                .contains(permanentId)) {
            throw new IllegalStateException("You can't gain control of that permanent for this cost");
        }
        destructionSupport.gainPermanentControl(gameData, gameQueryService.findPermanentById(gameData, permanentId),
                payerId, context.sourceCard().getName());
        if (context.remaining() > 1) {
            gainControlOrAsk(gameData, payerId, control, context.remaining() - 1, context.sourceCard(),
                    context.sourceControllerId());
        }
    }

    /** The payer returns the permanent they chose to its owner's hand (a {@link ReturnPermanentToHandCost} ransom). */
    private void completeReturnToHand(GameData gameData, UUID payerId, UUID permanentId,
                                      ReturnPermanentToHandCost toHand) {
        if (!returnCandidates(gameData, payerId, toHand, gameData.pendingEffectResolutionEntry).contains(permanentId)) {
            throw new IllegalStateException("That permanent can't be returned for this cost");
        }
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        if (permanentRemovalService.removePermanentToHand(gameData, chosen)) {
            permanentRemovalService.removeOrphanedAuras(gameData);
            gameLogService.append(gameData, GameLog.cardThen(chosen.getCard(), " is returned to its owner's hand."));
        }
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
            case SacrificeCreaturesWithTotalPowerCost power -> {
                List<UUID> candidates = powerCandidates(gameData, playerId, gameData.pendingEffectResolutionEntry);
                if (!candidates.containsAll(permanentIds)) {
                    throw new IllegalStateException("A selected creature can't be sacrificed for this cost");
                }
                int total = permanentIds.stream()
                        .map(id -> gameQueryService.findPermanentById(gameData, id))
                        .mapToInt(permanent -> gameQueryService.getEffectivePower(gameData, permanent))
                        .sum();
                if (total < power.totalPower()) {
                    // Not a partial payment: the selection reaches the total or it isn't one (CR 118.3).
                    throw new IllegalStateException("Selected creatures have total power below " + power.totalPower());
                }
            }
            case ReturnPermanentToHandCost toHand -> {
                if (permanentIds.size() != toHand.count()) {
                    throw new IllegalStateException("Must select exactly " + toHand.count() + " permanents");
                }
                if (!returnCandidates(gameData, playerId, toHand, gameData.pendingEffectResolutionEntry)
                        .containsAll(permanentIds)) {
                    throw new IllegalStateException("A selected permanent can't be returned for this cost");
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
                // Sacrificed at the same time, one event (CR 603.10a): each sees the others leave with it.
                destructionSupport.sacrificeTogether(gameData, chosen, playerId);
                permanentRemovalService.removeOrphanedAuras(gameData);
            }
            case SacrificeCreaturesWithTotalPowerCost ignored -> {
                List<UUID> chosen = permanentIds.stream()
                        .filter(id -> gameQueryService.findPermanentById(gameData, id) != null
                                && playerId.equals(gameQueryService.findPermanentController(gameData, id)))
                        .toList();
                // The chosen creatures are sacrificed at the same time (one event, CR 603.10a).
                destructionSupport.performSimultaneousSacrifice(gameData, chosen);
                permanentRemovalService.removeOrphanedAuras(gameData);
            }
            case ReturnPermanentToHandCost ignored -> returnToHandTogether(gameData, playerId, permanentIds);
            case WaterbendCost waterbend -> payWaterbend(gameData, playerId, waterbend, context.sourceCard(),
                    context.sourceControllerId(), permanentIds);
            default -> throw new IllegalStateException("No multi-permanent choice pays " + context.cost());
        }
    }

    /**
     * The payer returns the permanents they chose, still theirs, to their owners' hands by the one instruction, through
     * the shared bounce: no ability resolves between them.
     */
    private void returnToHandTogether(GameData gameData, UUID payerId, List<UUID> permanentIds) {
        List<Card> returned = new ArrayList<>();
        for (UUID permanentId : permanentIds) {
            Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
            if (chosen != null && payerId.equals(gameQueryService.findPermanentController(gameData, permanentId))
                    && permanentRemovalService.removePermanentToHand(gameData, chosen)) {
                returned.add(chosen.getCard());
            }
        }
        if (!returned.isEmpty()) {
            permanentRemovalService.removeOrphanedAuras(gameData);
            GameLog.Builder line = GameLog.builder().text(gameData.playerIdToName.get(payerId) + " returns ");
            for (int i = 0; i < returned.size(); i++) {
                if (i > 0) {
                    line.text(", ");
                }
                line.card(returned.get(i));
            }
            gameLogService.append(gameData, line.text(" to their owner's hand.").build());
        }
    }

    /**
     * The resolving entry's source while it is that permanent (CR 400.7) with {@code counters}' counters on it, from
     * which "remove two oil counters from it" removes them; else {@code null}.
     */
    private Permanent sourceWithCounters(GameData gameData, StackEntry entry, RemoveCounterFromSourceCost counters) {
        Permanent source = entry == null || entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        return source != null && source.getCounterCount(counters.counterType()) >= counters.count() ? source : null;
    }

    /** The resolving entry's source while it is that permanent and can get {@code counter}'s kind of counter. */
    private Permanent sourceForCounter(GameData gameData, StackEntry entry, PutTypedCounterOnSourceCost counter) {
        Permanent source = entry == null || entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || gameQueryService.cantHaveCounters(gameData, source)) {
            return null;
        }
        boolean blocked = switch (counter.counterType()) {
            case MINUS_ONE_MINUS_ONE -> gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, source);
            case PLUS_ONE_PLUS_ONE -> gameQueryService.cantHavePlusOnePlusOneCounters(gameData, source);
            default -> false;
        };
        return blocked ? null : source;
    }

    /** The graveyard indices of {@code payerId}'s cards {@code toHand} may return, judged from the entry's source. */
    private List<Integer> graveyardReturnIndices(GameData gameData, UUID payerId,
                                                 ReturnCardFromGraveyardToHandCost toHand, StackEntry entry) {
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(payerId, List.of());
        UUID sourceCardId = entry == null || entry.getCard() == null ? null : entry.getCard().getId();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < graveyard.size(); i++) {
            if (predicateEvaluationService.matchesCardPredicate(graveyard.get(i), toHand.predicate(), sourceCardId,
                    gameData, payerId)) {
                indices.add(i);
            }
        }
        return indices;
    }

    /**
     * Completes a {@link GraveyardTargetOperationState.ResolutionCostLibraryBottomContext}: the chosen cards, still in
     * the payer's graveyard, go to the bottom of their library. The caller ends through the epilogue, which resumes
     * the asking resolution.
     */
    public void completeLibraryBottom(GameData gameData, Player payer, List<UUID> cardIds) {
        GraveyardTargetOperationState.ResolutionCostLibraryBottomContext context =
                gameData.graveyardTargetOperation.resolutionCostLibraryBottom;
        gameData.graveyardTargetOperation.resolutionCostLibraryBottom = null;
        if (cardIds.size() != context.count()) {
            throw new IllegalStateException("Choose exactly " + context.count() + " cards");
        }
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(payer.getId(), List.of());
        putOnBottomOfLibrary(gameData, payer, graveyard.stream().filter(card -> cardIds.contains(card.getId()))
                .toList());
    }

    /** {@code cards} of {@code payer}'s graveyard put on the bottom of their library, in that order. */
    private void putOnBottomOfLibrary(GameData gameData, Player payer, List<Card> cards) {
        for (Card card : cards) {
            permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
            gameData.playerDecks.get(payer.getId()).addLast(card);
            gameLogService.append(gameData, GameLog.textCardText(payer.getUsername() + " puts ", card,
                    " on the bottom of their library."));
        }
    }

    /**
     * The counters {@code payerId} could remove for {@code counters}: those of an allowed kind on the permanents they
     * control that the cost allows, the entry's source left out when it says so (silver counters are on exiled cards,
     * never on permanents).
     */
    private int removableCounters(GameData gameData, UUID payerId, RemoveCounterFromControlledPermanentCost counters,
                                  StackEntry entry) {
        UUID sourceId = entry == null ? null : entry.getSourcePermanentId();
        return counterCandidates(gameData, payerId, counters, sourceId).stream()
                .mapToInt(permanent -> removableKinds(permanent, counters).stream()
                        .mapToInt(permanent::getCounterCount).sum())
                .sum();
    }

    private List<Permanent> counterCandidates(GameData gameData, UUID payerId,
                                              RemoveCounterFromControlledPermanentCost counters, UUID sourceId) {
        return gameData.playerBattlefields.getOrDefault(payerId, List.of()).stream()
                .filter(permanent -> !(counters.excludeSource() && permanent.getId().equals(sourceId)))
                .filter(permanent -> counters.permanentFilter() == null
                        || predicateEvaluationService.matchesPermanentPredicate(gameData, permanent,
                        counters.permanentFilter()))
                .filter(permanent -> !removableKinds(permanent, counters).isEmpty())
                .toList();
    }

    /** The kinds of counter on {@code permanent} that {@code counters} allows, in a fixed order. */
    private static List<CounterType> removableKinds(Permanent permanent,
                                                    RemoveCounterFromControlledPermanentCost counters) {
        return permanent.getCounters().entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .filter(kind -> kind != CounterType.ANY && kind != CounterType.SILVER && counters.allows(kind))
                .sorted()
                .toList();
    }

    /**
     * Removes the next of {@code remaining} counters for {@code counters}: all of them at once, on the first pick, when
     * one permanent with one allowed kind has them (nothing to choose), else the payer picks the permanent and kind of
     * the next one ({@link ChoiceContext.ResolutionCostCounterRemoval}).
     */
    private Outcome removeNextCounters(GameData gameData, UUID payerId,
                                       RemoveCounterFromControlledPermanentCost counters, UUID sourceId,
                                       int remaining, boolean firstPick) {
        List<Permanent> candidates = counterCandidates(gameData, payerId, counters, sourceId);
        if (firstPick && candidates.size() == 1 && removableKinds(candidates.getFirst(), counters).size() == 1) {
            Permanent only = candidates.getFirst();
            permanentCounterSupport.removeCounterFromPermanent(gameData, only,
                    removableKinds(only, counters).getFirst(), remaining);
            return afterImmediatePayment(gameData);
        }
        Map<String, ChoiceContext.CounterSelection> options = new java.util.LinkedHashMap<>();
        Map<String, Integer> names = new java.util.HashMap<>();
        candidates.forEach(permanent -> names.merge(FaceDownVisibility.publicName(permanent), 1, Integer::sum));
        Map<String, Integer> seen = new java.util.HashMap<>();
        for (Permanent permanent : candidates) {
            String name = FaceDownVisibility.publicName(permanent);
            int occurrence = seen.merge(name, 1, Integer::sum);
            String label = names.get(name) == 1 ? name : name + " (" + occurrence + ")";
            List<CounterType> kinds = removableKinds(permanent, counters);
            for (CounterType kind : kinds) {
                String kindLabel = ChoiceContext.AdjustChosenCounterTypeChoice.counterLabel(kind);
                options.put(kinds.size() == 1 ? label : candidates.size() == 1 ? kindLabel : label + " - " + kindLabel,
                        new ChoiceContext.CounterSelection(permanent.getId(), kind));
            }
        }
        var context = new ChoiceContext.ResolutionCostCounterRemoval(payerId, counters, sourceId, remaining, options);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(payerId, null, null, context,
                context.options(), "Choose a permanent to remove a counter from (" + remaining + " remaining)."));
        return Outcome.ASKING;
    }

    /**
     * Completes one pick of a {@link ChoiceContext.ResolutionCostCounterRemoval}: that counter is removed through the
     * counter action; while more are to be removed the next pick is asked (or the rest removed when nothing is left to
     * choose). Returns whether the payment is complete, after which the caller ends through the epilogue that resumes
     * the asking resolution.
     */
    public boolean completeCounterRemoval(GameData gameData, String option,
                                          ChoiceContext.ResolutionCostCounterRemoval context) {
        ChoiceContext.CounterSelection selection = context.permanentOptions().get(option);
        if (selection == null) {
            throw new IllegalArgumentException("Invalid permanent choice: " + option);
        }
        Permanent permanent = gameQueryService.findPermanentById(gameData, selection.permanentId());
        if (permanent == null || !context.payerId().equals(
                gameQueryService.findPermanentController(gameData, permanent.getId()))
                || !removableKinds(permanent, context.cost()).contains(selection.counterType())) {
            throw new IllegalArgumentException("That permanent can't pay this counter removal");
        }
        gameData.interaction.clearAwaitingInput();
        permanentCounterSupport.removeCounterFromPermanent(gameData, permanent, selection.counterType(), 1);
        int remaining = context.remaining() - 1;
        if (remaining <= 0) {
            return true;
        }
        return removeNextCounters(gameData, context.payerId(), context.cost(), context.sourcePermanentId(), remaining,
                false) != Outcome.ASKING;
    }

    /**
     * The permanents {@code payerId} could return for {@code toHand}: their own matching ones (any player's when the
     * cost says so), judged by current characteristics from the entry's source, so "another" leaves the source out;
     * never one that can't be chosen.
     */
    private List<UUID> returnCandidates(GameData gameData, UUID payerId, ReturnPermanentToHandCost toHand,
                                        StackEntry entry) {
        FilterContext context = filterContext(gameData, entry);
        List<Permanent> permanents = new ArrayList<>();
        if (toHand.anyController()) {
            gameData.playerBattlefields.values().forEach(permanents::addAll);
        } else {
            permanents.addAll(gameData.playerBattlefields.getOrDefault(payerId, List.of()));
        }
        return permanents.stream()
                .filter(permanent -> toHand.filter() == null || (context == null
                        ? predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, toHand.filter())
                        : predicateEvaluationService.matchesPermanentPredicate(permanent, toHand.filter(), context)))
                .filter(permanent -> !gameQueryService.cantBeChosen(gameData, permanent))
                .map(Permanent::getId)
                .toList();
    }

    /** The creatures {@code payerId} could sacrifice for a total-power ransom (CR 608.2d, 701.21a). */
    private List<UUID> powerCandidates(GameData gameData, UUID payerId, StackEntry entry) {
        return sacrificeCandidates(gameData, payerId, new PermanentIsCreaturePredicate(), false, entry);
    }

    /** The greatest total power a choice among {@code candidates} can have: every positive power. */
    private int reachablePower(GameData gameData, List<UUID> candidates) {
        return candidates.stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .mapToInt(permanent -> Math.max(0, gameQueryService.getEffectivePower(gameData, permanent)))
                .sum();
    }

    /** Whether {@code playerId} could give up a land drop now ({D}): on their own turn, with one left. */
    private boolean hasLandDropLeft(GameData gameData, UUID playerId) {
        return playerId.equals(gameData.activePlayerId)
                && gameData.landsPlayedThisTurn.getOrDefault(playerId, 0)
                < gameQueryService.getMaxLandsThisTurn(gameData, playerId);
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

    /** "a creature", "an Island"; a description that reads with its determiner ("another creature") as it is. */
    private static String withArticle(String noun) {
        if (noun.startsWith("another ") || noun.startsWith("a ") || noun.startsWith("an ")) {
            return noun;
        }
        return ("aeiou".indexOf(Character.toLowerCase(noun.charAt(0))) >= 0 ? "an " : "a ") + noun;
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

    /**
     * {@code cost} with {@code x} chosen for its X (CR 107.3f), as the mana cost then paid: mana spendable only on costs
     * with {X} may pay it, {B} with 2 life under K'rrik, and paying it records the {@code PAY_MANA} step and the tax as
     * any resolution-time mana payment ({@link #pay}).
     */
    public static PayManaCost paidWith(PayXManaCost cost, int x) {
        return new PayManaCost(cost.withX(x), null, null, false, 0, false, true);
    }

    /**
     * The most X {@code cost} allows, its limit read already ({@link Fixed}), or {@link Integer#MAX_VALUE} without
     * one.
     *
     * @throws IllegalArgumentException for a limit not read yet
     */
    public static int maximumX(PayXManaCost cost) {
        if (cost.maximumX() == null) {
            return Integer.MAX_VALUE;
        }
        if (cost.maximumX() instanceof Fixed fixed) {
            return Math.max(0, fixed.value());
        }
        throw new IllegalArgumentException("Read the limit of X before paying it: " + cost);
    }

    /**
     * The most X {@code payerId} could pay {@code cost} with, from their pool and the mana their untapped sources could
     * still add (they may activate mana abilities while asked, CR 605.3a), as a resolution-time cost: Powerstone mana
     * and mana spendable only on costs with {X} count. An estimate for the question's upper bound; the payment checks the
     * pool.
     */
    public int mostXPayable(GameData gameData, UUID payerId, PayXManaCost cost) {
        ManaCost mana = new ManaCost(cost.manaCost());
        ManaPool pool = gameData.playerManaPools.get(payerId);
        int most = pool == null ? 0 : mana.calculateMaxXForResolutionCost(pool);
        if (potentialManaService != null) {
            ManaPool potential = potentialManaService.buildVirtualManaPoolForSpecialAction(gameData, payerId);
            potential.addXCostOnlyColorless((pool == null ? 0 : pool.getXCostOnlyColorless())
                    + potentialManaService.potentialXCostOnlyMana(gameData, payerId));
            most = Math.max(most, mana.calculateMaxXForResolutionCost(potential));
        }
        return Math.min(most, maximumX(cost));
    }

    /**
     * Asks {@code payerId} for the X of {@code cost} (CR 107.3f): one number from 0 (not paying) to
     * {@link #mostXPayable}, at least 1 as they are asked only when they could possibly pay X = 1; a mana payment, so
     * they may activate mana abilities while asked (CR 605.3a). The answer resumes the waiting resolution.
     */
    public void askX(GameData gameData, UUID payerId, PayXManaCost cost, String prompt, String sourceName) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(payerId, 0,
                Math.max(1, mostXPayable(gameData, payerId, cost)), prompt, sourceName, true, cost.manaCost()));
    }

    /** {@code mana}'s cost as a {@link ManaCost}, or {@code null} for a life-only payment. */
    public static ManaCost manaCostOf(PayManaCost mana) {
        if (mana.genericReduction() != null || mana.genericIncrease() != null || mana.useSourceManaCost()
                || mana.timesPaid() != null) {
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
        ManaCost cost = phyrexianPayment(gameData, payerId, manaCostOf(mana), potential);
        if (cost == UNPAYABLE) {
            return -1;
        }
        int lifeForPhyrexian = cost != null && cost.hasPhyrexianMana()
                ? cost.payPhyrexianManaAuto(new ManaPool(gameData.playerManaPools.get(payerId)), 0) : 0;
        int life = mana.lifeAmount() + lifeForPhyrexian;
        return canPayLife(gameData, payerId, life) ? life : -1;
    }

    /** {@link #phyrexianPayment}'s answer for mana that can't be paid. */
    private static final ManaCost UNPAYABLE = new ManaCost("{0}");

    /**
     * {@code cost} as {@code payerId} pays it: its Phyrexian symbols with their colour, else 2 life each (the mana check
     * leaves them out; CR 107.4f), {B} with 2 life under K'rrik, Son of Yawgmoth when the mana falls short; {@link #UNPAYABLE} when the mana can't be paid,
     * {@code null} for no mana.
     */
    private ManaCost phyrexianPayment(GameData gameData, UUID payerId, ManaCost cost, boolean potential) {
        if (cost == null) {
            return null;
        }
        if (canPayManaNow(gameData, payerId, cost, potential)) {
            return cost;
        }
        ManaCost blackAsLife = cost.withBlackManaAsPhyrexian();
        if (blackAsLife == cost || !gameQueryService.canPayBlackManaWithLife(gameData, payerId)
                || !canPayManaNow(gameData, payerId, blackAsLife, potential)) {
            return UNPAYABLE;
        }
        return blackAsLife;
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
        ManaCost toPay = phyrexianPayment(gameData, payerId, manaCostOf(mana), false);
        ManaPool pool = gameData.playerManaPools.get(payerId);
        Runnable payManaThenLife = () -> {
            if (toPay != null) {
                int before = pool.getTotalAllMana();
                Map<ManaColor, Integer> typesBefore = pool.getAllManaTotals();
                if (toPay.hasPhyrexianMana()) {
                    toPay.payPhyrexianManaAuto(pool, 0);
                }
                toPay.payResolutionCost(pool);
                ActionEvents.taxPaid(gameData, payerId, payment.sourceControllerId(), before - pool.getTotalAllMana());
                recordManaPaid(payment.entry(), payerId, typesBefore, pool.getAllManaTotals());
            }
            if (life > 0) {
                lifeSupport.applyLifePayment(gameData, payerId, life, payment.sourceCard().getName());
            }
        };
        return payAskingDeathReplacementsFirst(gameData, payer, life, payment, payManaThenLife);
    }

    /**
     * Records the mana {@code payerId} just spent as the resolving entry's {@code PAY_MANA} step, in all and of each type
     * ("for each {B} or {R} spent this way": Balduvian Fallen's cumulative upkeep), so a later step or the reflexive
     * ability the payment makes reads it.
     */
    private static void recordManaPaid(StackEntry entry, UUID payerId, Map<ManaColor, Integer> before,
                                       Map<ManaColor, Integer> after) {
        if (entry == null) {
            return;
        }
        Map<ManaColor, Integer> spent = new java.util.EnumMap<>(ManaColor.class);
        before.forEach((color, amount) -> {
            int paid = amount - after.getOrDefault(color, 0);
            if (paid > 0) {
                spent.put(color, paid);
            }
        });
        entry.recordStep(StepKind.PAY_MANA, StepOutcome.ofManaPaid(payerId, spent));
    }

    private Outcome payLife(GameData gameData, Player payer, int life, Payment payment) {
        UUID payerId = payer.getId();
        if (!canPayLife(gameData, payerId, life)) {
            return Outcome.NOT_PAID;
        }
        return payAskingDeathReplacementsFirst(gameData, payer, life, payment, () -> {
            lifeSupport.applyLifePayment(gameData, payerId, life, payment.sourceCard().getName());
            // "That many" after "you may pay life equal to …" reads the life paid (Madame Null, Power Broker).
            if (payment.entry() != null) {
                payment.entry().recordStep(StepKind.PAY_LIFE, StepOutcome.ofAmounts(Map.of(payerId, life)));
            }
        });
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
