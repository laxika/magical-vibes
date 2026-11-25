package com.github.laxika.magicalvibes.service.effect.cost;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PlayerInteractionSupport;
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
 * A caller asks {@link #canPay} (CR 118.3: a cost can't be paid without the resources to pay it fully) and, once the
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
 * </ul>
 * A payment with a choice ({@link Outcome#ASKING}) is finished by that prompt's completion, which ends through its own
 * epilogue; a life payment that a Lich's Duel Mastery sacrifice would follow asks that sacrifice's death replacements
 * first (CR 616.1) and completes the answer again once chosen.
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
     */
    public record Payment(Card sourceCard, UUID sourceControllerId, PendingMayAbility answering) {
    }

    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;
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
        return switch (cost) {
            case PayManaCost mana -> lifeForMana(gameData, payerId, mana, true) >= 0;
            case PayLifeCost life -> canPayLife(gameData, payerId, fixedLife(life));
            case DiscardCardTypeCost discard ->
                    discardableIndices(gameData, payerId, discard, sourceCard).size() >= discard.count();
            case DiscardRandomCardCost discard -> handSize(gameData, payerId) >= discard.count();
            case SacrificePermanentCost sacrifice -> !sacrificeCandidates(gameData, payerId, sacrifice).isEmpty();
            default -> throw new IllegalArgumentException("No resolution-time payment for " + cost);
        };
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
                List<UUID> candidates = sacrificeCandidates(gameData, payerId, sacrifice);
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
            default -> throw new IllegalArgumentException("No resolution-time payment for " + cost);
        };
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

    private List<UUID> sacrificeCandidates(GameData gameData, UUID payerId, SacrificePermanentCost sacrifice) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(payerId);
        if (battlefield == null) {
            return List.of();
        }
        return battlefield.stream()
                .filter(permanent -> sacrifice.filter() == null
                        || predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, sacrifice.filter()))
                .map(Permanent::getId)
                .toList();
    }
}
