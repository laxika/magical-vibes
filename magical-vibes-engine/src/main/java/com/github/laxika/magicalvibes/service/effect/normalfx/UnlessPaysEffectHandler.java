package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.UnlessAnswer;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AllOfCost;
import com.github.laxika.magicalvibes.model.effect.AnyOfCost;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceCost;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.DamageFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DiscardHandCost;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;
import com.github.laxika.magicalvibes.model.effect.ExileCardsCost;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GetPoisonCountersCost;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.WaterbendCost;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.effect.cost.ResolutionCostPaymentService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * "[Do something] unless [a player pays a cost]" (CR 118.12a). The payer is asked as the effect resolves, the
 * resolution waiting on this effect; the answer ({@code MayAbilityHandlerService}) pays the cost and records the
 * payer's choice on the resolving entry ({@link StackEntry#answerUnless}), and the resolution runs this effect again,
 * which {@code EffectResolutionService} then resolves as the branch the choice made ({@code ifPaid} or
 * {@code ifNotPaid}, CR 118.12: the choice to pay, not what happened). The choice is the entry's, not the game's, so a
 * question another ability asks while a payment's own choice is made (which card to discard, which creature to blight)
 * can't take it. A payer who has no way at all to pay the cost isn't asked and {@code ifNotPaid} resolves at once
 * ({@link #cannotBePaid}, {@link ResolutionCostPaymentService#couldPossiblyPay}, CR 118.3, 608.2d); one who might
 * still make the mana is asked, and an answer to pay without the resources pays nothing (CR 118.3). "Do this only
 * once each turn" (CR 603.2h): once its action was taken this turn, the payment can't be made again, and a paid answer
 * takes it.
 *
 * <p>An any-of ransom ("discard a card or pay {2}") is asked one alternative at a time, in its order, skipping those
 * the payer couldn't pay; declining one asks the next, and declining every one is not paying. An all-of ransom ("pay
 * {1} and discard a card") is one question; accepted, it is paid only when every part can be paid from what the payer
 * has (CR 601.2h, 118.3), the mana first, then the other parts in Oracle order, a part that asks finishing before the
 * next is paid ({@link UnlessAnswer#partsToPay}).
 *
 * <p>Every player the payer subject names is asked, in APNAP order, every one even after another paid (CR 101.4: "any
 * player may pay"); the state is the entry's {@link UnlessAnswer}, the branch is chosen once after the last answer
 * ({@link #nextStep}): {@code ifPaid} if any of them paid. Mana paid for another player's object is a tax paid to that
 * player, payer by payer.
 *
 * <p>What can pay: the payer's mana pool and the mana they could still make (they may activate mana abilities while
 * asked, CR 605.3a); mana that can be spent only on {X} for an announced-X ransom; 2 life for each {B} under K'rrik,
 * Son of Yawgmoth; life only while the payer can lose life (CR 119.4, 119.8); the other costs as
 * {@link ResolutionCostPaymentService} judges and pays them, through the shared actions. An amount read from the source
 * reads the source on the battlefield, else as it last existed (CR 608.2h).
 */
@Slf4j
@Component
public class UnlessPaysEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final ResolutionCostPaymentService costPayment;

    @Autowired
    public UnlessPaysEffectHandler(SubjectResolutionService subjectResolutionService,
                                   AmountEvaluationService amountEvaluationService,
                                   GameQueryService gameQueryService, GameLogService gameLogService,
                                   ResolutionCostPaymentService costPayment) {
        this.subjectResolutionService = subjectResolutionService;
        this.amountEvaluationService = amountEvaluationService;
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.costPayment = costPayment;
    }

    /**
     * A handler outside a Spring context, paying through a {@link ResolutionCostPaymentService} of its own: that one
     * counts only the mana in the payer's pool, as it has no mana abilities to look at.
     */
    public UnlessPaysEffectHandler(SubjectResolutionService subjectResolutionService,
                                   AmountEvaluationService amountEvaluationService,
                                   GameQueryService gameQueryService, GameLogService gameLogService,
                                   LifeSupport lifeSupport) {
        this(subjectResolutionService, amountEvaluationService, gameQueryService, gameLogService,
                new ResolutionCostPaymentService(gameQueryService, lifeSupport));
    }

    /**
     * One way to pay the ransom as it is paid now (every amount read), with its words.
     *
     * @param cost     the cost {@link ResolutionCostPaymentService} pays
     * @param action   the imperative the prompt asks ("Pay {2}", "Discard a card", "Blight 2")
     * @param done     what the log says the payer does ("pays {2}", "discards a card")
     * @param declined what the log says the payer chooses or declines to do ("pay {2}", "discard a card")
     */
    public record Ransom(CostEffect cost, String action, String done, String declined, boolean unpayable) {

        public Ransom(CostEffect cost, String action, String done, String declined) {
            this(cost, action, done, declined, false);
        }

        /** A cost that can't be paid (CR 118.6): the mana cost of a spell that has none, or a face-down spell's. */
        static Ransom unpayableMana() {
            return new Ransom(new PayManaCost("{0}"), "Pay its mana cost", "pays its mana cost", "pay its mana cost",
                    true);
        }

        /** A mana and / or life ransom: {@code manaCost} ({@code null} for none), X-only mana, and the life. */
        static Ransom ofManaAndLife(String manaCost, boolean xCost, int life) {
            String text = manaCost == null ? life + " life" : life > 0 ? manaCost + " and " + life + " life" : manaCost;
            CostEffect cost = manaCost == null ? new PayLifeCost(life)
                    : new PayManaCost(manaCost, null, null, false, life, false, xCost);
            return new Ransom(cost, "Pay " + text, "pays " + text, "pay " + text);
        }

        /** The mana to pay (an all-of ransom's mana part), or {@code null} when the ransom isn't (or has no) mana. */
        public String manaCost() {
            PayManaCost mana = cost instanceof AllOfCost allOf ? allOf.manaPart()
                    : cost instanceof PayManaCost single ? single : null;
            return mana != null && mana.manaCost() != null && !mana.manaCost().isEmpty() ? mana.manaCost() : null;
        }

        /** Whether mana spendable only on {X} may pay it. */
        public boolean xCost() {
            return cost instanceof PayManaCost mana && mana.announcedX();
        }

        /** The life paid with or instead of mana. */
        public int life() {
            return switch (cost) {
                case PayManaCost mana -> mana.lifeAmount();
                case PayLifeCost life -> ((Fixed) life.amount()).value();
                default -> 0;
            };
        }

        /** Whether this is a mana or life payment, whose answer is read from the question as asked. */
        public boolean manaOrLife() {
            return cost instanceof PayManaCost || cost instanceof PayLifeCost;
        }

        /** The cost as the mana and life prompts and log lines read it: "{2}", "3 life", "{2} and 2 life". */
        public String text() {
            return manaOrLife() ? action.substring("Pay ".length()) : action;
        }
    }

    /** What the resolution does with an "unless" effect before it asks anyone ({@link #nextStep}). */
    public enum Step {
        /** The first remaining payer is asked ({@link #resolve}). */
        ASK,
        /** Every payer answered and one chose to pay: {@code ifPaid}. */
        PAID,
        /** Every payer answered (or could pay in no way at all) and none paid: {@code ifNotPaid}. */
        NOT_PAID,
        /** Nobody is asked and nothing happens: the spell the effect is about left the stack. */
        NOTHING,
        /** A part of an all-of ransom asks the payer a choice; the resolution waits on this effect for it. */
        PAYING
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UnlessPaysEffect.class;
    }

    /**
     * Asks the first remaining payer of {@code effect}, the payer list {@link #nextStep} prepared (APNAP order, every
     * payer who has no way at all to pay left out).
     */
    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var unless = (UnlessPaysEffect) effect;
        int index = entry.getResolvingEffectIndex();
        if (entry.unlessAnswer(index) == null && nextStep(gameData, entry, unless, index) != Step.ASK) {
            return;
        }
        UnlessAnswer state = entry.unlessAnswer(index);
        if (state == null || state.currentPayer() == null) {
            return;
        }
        UUID payerId = state.currentPayer();
        List<Ransom> alternatives = alternatives(gameData, entry, unless);
        int asked = Math.min(state.alternative(), alternatives.size() - 1);
        Ransom ransom = alternatives.get(asked);
        StackEntry spell = spellAskedAbout(gameData, entry, unless);
        String prompt = unless.prompt() != null && alternatives.size() == 1 ? unless.prompt()
                : ransom.action() + suffix(gameData, entry, unless, spell, payerId) + "?";
        gameData.resolvingMayEffectFromStack = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), payerId, List.of(unless), prompt,
                spell == null ? entry.getTargetId() : spell.getTargetableId(), ransom.manaCost(),
                entry.getSourcePermanentId(), null, 0, ransom.life(), null, null, null,
                entry.getSourcePermanentSnapshot(), entry.getControllerId()));
    }

    /**
     * Where the "unless" effect at {@code index} goes next as the resolution reaches it: on with the parts of an
     * all-of ransom still to pay ({@link Step#PAYING} while one asks); then, the payers named once on the first run
     * ({@code players(payer)}, APNAP, CR 101.4), each player who left the game or has no way at all to pay
     * ({@link ResolutionCostPaymentService#couldPossiblyPay}, CR 118.3, 608.2d) dropped, the first remaining one
     * asked ({@link Step#ASK}); with nobody left, the branch: {@code ifPaid} when one of them chose to pay, else
     * {@code ifNotPaid} (CR 118.12). A "do this only once each turn" action already taken this turn makes the payment
     * impossible (CR 603.2h).
     */
    public Step nextStep(GameData gameData, StackEntry entry, UnlessPaysEffect unless, int index) {
        UnlessAnswer state = entry.unlessAnswer(index);
        if (state != null && !state.partsToPay().isEmpty()) {
            UUID payingId = state.payingPlayerId();
            Player payer = new Player(payingId, gameData.playerIdToName.get(payingId));
            if (payParts(gameData, entry, index, payer, state.partsToPay(), new ResolutionCostPaymentService.Payment(
                    entry.getCard(), entry.getControllerId(), null, entry))
                    == ResolutionCostPaymentService.Outcome.ASKING) {
                return Step.PAYING;
            }
            state = entry.unlessAnswer(index);
        }
        if (state == null) {
            List<UUID> payers = payers(gameData, entry, unless);
            if (payers == null) {
                return Step.NOTHING;
            }
            state = UnlessAnswer.asking(index, payers);
        }
        boolean actionTaken = onceEachTurnActionTaken(gameData, entry);
        List<Ransom> alternatives = alternatives(gameData, entry, unless);
        while (state.currentPayer() != null) {
            UUID head = state.currentPayer();
            int payable = actionTaken || !gameData.playerIds.contains(head) ? -1
                    : firstPayable(gameData, entry, head, alternatives, state.alternative());
            if (payable >= 0) {
                state = state.askingAlternative(payable);
                break;
            }
            state = state.answered(false);
        }
        if (state.currentPayer() == null) {
            entry.clearUnlessAnswer(index);
            return state.anyPaid() ? Step.PAID : Step.NOT_PAID;
        }
        entry.setUnlessAnswer(state);
        return Step.ASK;
    }

    /**
     * Whether none of {@code unless}'s payers could pay its ransom in any way now, so none would be asked and its
     * {@code ifNotPaid} happens (CR 118.3, 608.2d). It changes nothing; the resolution decides through
     * {@link #nextStep}.
     */
    public boolean cannotBePaid(GameData gameData, StackEntry entry, UnlessPaysEffect unless) {
        List<UUID> payers = payers(gameData, entry, unless);
        if (payers == null) {
            return false;
        }
        if (onceEachTurnActionTaken(gameData, entry)) {
            return true;
        }
        List<Ransom> alternatives = alternatives(gameData, entry, unless);
        return payers.stream().noneMatch(payerId -> firstPayable(gameData, entry, payerId, alternatives, 0) >= 0);
    }

    /**
     * Whether a player already chose to pay the "unless" payment the waiting resolution asks about: another payment by
     * the next player asked changes nothing (an AI declines it).
     */
    public static boolean anotherPlayerAlreadyPaid(GameData gameData) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        int index = gameData.pendingEffectResolutionIndex;
        if (entry == null || index < 0 || index >= entry.getEffectsToResolve().size()
                || !(entry.getEffectsToResolve().get(index) instanceof UnlessPaysEffect)) {
            return false;
        }
        UnlessAnswer state = entry.unlessAnswer(index);
        return state != null && state.anyPaid();
    }

    /** Whether the entry's "do this only once each turn" action was already taken this turn (CR 603.2h). */
    private static boolean onceEachTurnActionTaken(GameData gameData, StackEntry entry) {
        return entry.marksOnceEachTurnAction() && TriggeredAbilityStackService.actionTaken(gameData, entry);
    }

    /**
     * The players who may pay, in APNAP order (CR 101.4), or {@code null}: none when the spell or ability the branch
     * is about is no longer on the stack (an earlier instruction countered it "instead", Bring the Ending), as there
     * is nothing left to pay for.
     */
    private List<UUID> payers(GameData gameData, StackEntry entry, UnlessPaysEffect unless) {
        if ((unless.counters() || unless.retargets()) && spellAskedAbout(gameData, entry, unless) == null) {
            return null;
        }
        return subjectResolutionService.players(gameData, entry, unless, unless.payer());
    }

    /** The spell or ability the counter or the retarget of {@code unless} acts on, while it is on the stack. */
    private StackEntry spellAskedAbout(GameData gameData, StackEntry entry, UnlessPaysEffect unless) {
        PermanentSubject spell = unless.counters() ? unless.counteredSpell()
                : unless.retargets() ? Subjects.targetSpell() : null;
        return spell == null ? null : subjectResolutionService.spellOnStack(gameData, entry, spell);
    }

    private static String suffix(GameData gameData, StackEntry entry, UnlessPaysEffect unless, StackEntry spell,
                                 UUID viewerId) {
        if (spell == null) {
            if (unless.countersIfPaid()) {
                StackEntry triggering = triggeringSpell(gameData, entry);
                return triggering == null ? ""
                        : " to counter " + FaceDownVisibility.nameShownTo(gameData, triggering, viewerId);
            }
            return "";
        }
        String name = FaceDownVisibility.nameShownTo(gameData, spell, viewerId);
        return unless.counters() ? " to prevent " + name + " from being countered"
                : " to keep " + name + " from being retargeted";
    }

    /** The spell whose casting triggered the entry: on the stack, else as it last was there. */
    private static StackEntry triggeringSpell(GameData gameData, StackEntry entry) {
        UUID triggeringCardId = entry.getTriggeringCardId();
        return gameData.stack.stream()
                .filter(stackEntry -> triggeringCardId != null && triggeringCardId.equals(stackEntry.getTargetableId()))
                .findFirst()
                .orElse(entry.getTriggeringSpellSnapshot());
    }

    private int firstPayable(GameData gameData, StackEntry entry, UUID payerId, List<Ransom> alternatives,
                             int from) {
        for (int i = Math.max(0, from); i < alternatives.size(); i++) {
            Ransom ransom = alternatives.get(i);
            if (!ransom.unpayable() && costPayment.couldPossiblyPay(gameData, payerId, ransom.cost(),
                    entry.getCard(), entry)) {
                return i;
            }
        }
        return -1;
    }

    /** {@code unless}'s ransom with its amounts read as it resolves; an any-of ransom's alternatives in order. */
    public List<Ransom> alternatives(GameData gameData, StackEntry entry, UnlessPaysEffect unless) {
        if (unless.ransom() instanceof AnyOfCost anyOf) {
            return anyOf.alternatives().stream().map(cost -> ransom(gameData, entry, cost)).toList();
        }
        return List.of(ransom(gameData, entry, unless.ransom()));
    }

    /** {@code unless}'s ransom with its amounts read as it resolves (the first alternative of an any-of ransom). */
    public Ransom ransom(GameData gameData, StackEntry entry, UnlessPaysEffect unless) {
        return alternatives(gameData, entry, unless).getFirst();
    }

    private Ransom ransom(GameData gameData, StackEntry entry, CostEffect cost) {
        return switch (cost) {
            case PayManaCost mana when mana.useTriggeringSpellManaCost() -> {
                String manaCost = triggeringSpellManaCost(gameData, entry);
                yield manaCost == null ? Ransom.unpayableMana() : Ransom.ofManaAndLife(manaCost, false, 0);
            }
            case PayManaCost mana -> Ransom.ofManaAndLife(manaCost(gameData, entry, mana), mana.announcedX(),
                    mana.lifeAmount());
            case PayLifeCost life -> Ransom.ofManaAndLife(null, false, Math.max(0,
                    amountEvaluationService.evaluate(gameData, life.amount(), amountContext(gameData, entry))));
            case DiscardCardTypeCost discard -> {
                String card = discard.label() == null ? "a card" : article(discard.label()) + " card";
                yield new Ransom(cost, "Discard " + card, "discards " + card, "discard " + card);
            }
            case DiscardRandomCardCost ignored ->
                    new Ransom(cost, "Discard a card at random", "discards a card at random",
                            "discard a card at random");
            case DiscardHandCost ignored ->
                    new Ransom(cost, "Discard your hand", "discards their hand", "discard their hand");
            case SacrificePermanentCost sacrifice -> {
                String what = article(sacrifice.description() == null ? "permanent" : sacrifice.description());
                yield new Ransom(cost, "Sacrifice " + what, "sacrifices " + what, "sacrifice " + what);
            }
            case SacrificeMultiplePermanentsCost sacrifice -> {
                String what = sacrifice.count() + " "
                        + (sacrifice.description() == null ? "permanents" : sacrifice.description());
                yield new Ransom(cost, "Sacrifice " + what, "sacrifices " + what, "sacrifice " + what);
            }
            case PutCounterOnControlledCreatureCost blight -> new Ransom(cost, "Blight " + blight.count(),
                    "blights " + blight.count(), "blight " + blight.count());
            case CollectEvidenceCost evidence -> new Ransom(cost, "Collect evidence " + evidence.minimumManaValue(),
                    "collects evidence " + evidence.minimumManaValue(),
                    "collect evidence " + evidence.minimumManaValue());
            case ExileGraveyardCost ignored -> new Ransom(cost, "Exile all cards from your graveyard",
                    "exiles all cards from their graveyard", "exile all cards from their graveyard");
            case GetPoisonCountersCost poison -> {
                String counters = poison.count() + " poison counter" + (poison.count() == 1 ? "" : "s");
                yield new Ransom(cost, "Get " + counters, "gets " + counters, "get " + counters);
            }
            case DamageFromSourceCost damage -> {
                int amount = amountEvaluationService.evaluate(gameData, damage.amount(), amountContext(gameData, entry));
                String dealer = FaceDownVisibility.sourceName(gameData, entry);
                yield new Ransom(new DamageFromSourceCost(new Fixed(amount)),
                        "Have " + dealer + " deal " + amount + " damage to you",
                        "has " + dealer + " deal " + amount + " damage to them",
                        "have " + dealer + " deal " + amount + " damage to them");
            }
            case WaterbendCost waterbend -> {
                String text = "{" + waterbend.amount() + "} using waterbend";
                yield new Ransom(cost, "Pay " + text, "pays " + text, "pay " + text);
            }
            case SacrificeSelfCost ignored -> {
                String source = FaceDownVisibility.sourceName(gameData, entry);
                yield new Ransom(cost, "Sacrifice " + source, "sacrifices " + source, "sacrifice " + source);
            }
            case ExileNCardsFromGraveyardCost exile -> {
                String cards = exile.count() == 1 ? "a card" : exile.count() + " cards";
                yield new Ransom(cost, "Exile " + cards + " from your graveyard",
                        "exiles " + cards + " from their graveyard", "exile " + cards + " from their graveyard");
            }
            case TapMultiplePermanentsCost tap -> {
                int count = tap.count() instanceof Fixed fixed ? fixed.value() : 1;
                String what = count == 1 ? "an untapped permanent" : count + " untapped permanents";
                yield new Ransom(cost, "Tap " + what + " you control", "taps " + what,
                        "tap " + what);
            }
            case ExileCardsCost ignored -> new Ransom(cost, "Exile that card", "exiles that card",
                    "exile that card");
            case AllOfCost allOf -> {
                List<Ransom> parts = allOf.parts().stream().map(part -> ransom(gameData, entry, part)).toList();
                if (parts.stream().anyMatch(Ransom::unpayable)) {
                    yield Ransom.unpayableMana();
                }
                yield new Ransom(new AllOfCost(parts.stream().map(Ransom::cost).toList()),
                        joined(parts.stream().map(Ransom::action).toList()),
                        joined(parts.stream().map(Ransom::done).toList()),
                        joined(parts.stream().map(Ransom::declined).toList()));
            }
            default -> throw new IllegalStateException("No resolution-time payment for the ransom " + cost);
        };
    }

    /** "Pay {1}" and "Discard a card" as "Pay {1} and discard a card". */
    private static String joined(List<String> phrases) {
        StringBuilder text = new StringBuilder(phrases.getFirst());
        for (String phrase : phrases.subList(1, phrases.size())) {
            text.append(" and ").append(Character.toLowerCase(phrase.charAt(0))).append(phrase.substring(1));
        }
        return text.toString();
    }

    /**
     * "That spell's mana cost": the triggering spell's, on the stack or as it last was there, its announced X
     * included, no increase or reduction applied (Ice Cave's ruling), or {@code null} when it can't be paid: a spell
     * cast face down has no mana cost (CR 708.2), and no mana cost can't be paid (CR 118.6).
     */
    private static String triggeringSpellManaCost(GameData gameData, StackEntry entry) {
        StackEntry spell = triggeringSpell(gameData, entry);
        if (spell == null || spell.isCastFaceDown() || spell.getCard().getManaCost() == null
                || spell.getCard().getManaCost().isEmpty()) {
            return null;
        }
        return spell.getCard().getManaCost().replace("{X}", "{" + spell.getXValue() + "}");
    }

    private static String article(String noun) {
        return ("aeiou".indexOf(Character.toLowerCase(noun.charAt(0))) >= 0 ? "an " : "a ") + noun;
    }

    private String manaCost(GameData gameData, StackEntry entry, PayManaCost mana) {
        if (mana.announcedX()) {
            return "{" + entry.getXValue() + "}";
        }
        if (mana.genericIncrease() == null && mana.genericReduction() == null) {
            return mana.manaCost();
        }
        AmountContext context = amountContext(gameData, entry);
        int delta = mana.genericIncrease() == null ? 0
                : amountEvaluationService.evaluate(gameData, mana.genericIncrease(), context);
        if (mana.genericReduction() != null) {
            delta -= amountEvaluationService.evaluate(gameData, mana.genericReduction(), context);
        }
        ManaCost base = new ManaCost(mana.manaCost());
        StringBuilder cost = new StringBuilder();
        int generic = Math.max(0, base.getGenericCost() + delta);
        if (generic > 0 || base.getColoredCosts().isEmpty()) {
            cost.append('{').append(generic).append('}');
        }
        base.getColoredCosts().forEach((color, count) -> cost.append(("{" + color.getCode() + "}").repeat(count)));
        return cost.toString();
    }

    /** An amount read from the source reads it on the battlefield, else as it last existed (CR 608.2h). */
    private AmountContext amountContext(GameData gameData, StackEntry entry) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        return AmountContext.forStackEntry(entry, source != null ? source : entry.getSourcePermanentSnapshot());
    }

    /** Whether {@code playerId} could pay {@code amount} life (CR 119.4, 119.8): {@link ResolutionCostPaymentService#canPayLife}. */
    public boolean canPayLife(GameData gameData, UUID playerId, int amount) {
        return costPayment.canPayLife(gameData, playerId, amount);
    }

    /**
     * {@code player}'s answer to {@code ability}, the question {@link #resolve} asked for the resolution waiting in
     * {@code gameData.pendingEffectResolutionEntry}. Accepted, the choice to pay is recorded on that entry and the
     * ransom paid: at once (the mana from their pool, {B} with 2 life under K'rrik when the mana falls short, then the
     * life; a random discard; poison counters; the damage), or by a prompt whose completion resumes the resolution
     * (the card to discard, the permanents to sacrifice, the creature to blight, the evidence, the waterbend taps).
     * Declined, or accepted without the resources (CR 118.3), the next alternative of an any-of ransom is asked, else
     * the choice not to pay is recorded. Paying life is losing it (CR 119.4): a Lich's Duel Mastery sacrifice it would
     * cause has its death replacements chosen first (CR 616.1), and the answer is then completed again.
     *
     * @return {@code true} when the resolution goes on now, {@code false} while a prompt finishes the payment
     */
    public boolean answer(GameData gameData, Player player, boolean accepted, PendingMayAbility ability,
                          UnlessPaysEffect unless) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("No resolution waits for the answer to " + ability.description());
        }
        int index = gameData.pendingEffectResolutionIndex;
        UnlessAnswer state = entry.unlessAnswer(index);
        if (state == null || !player.getId().equals(state.currentPayer())) {
            throw new IllegalStateException(player.getUsername() + " isn't asked to pay for "
                    + ability.description());
        }
        List<Ransom> alternatives = alternatives(gameData, entry, unless);
        int asked = Math.min(state.alternative(), alternatives.size() - 1);
        Ransom ransom = alternatives.get(asked);
        if (ransom.manaOrLife()) {
            // The mana and life as the question carried them: the payer may have tapped for it while asked.
            ransom = Ransom.ofManaAndLife(ability.manaCost(), ransom.xCost(), ability.additionalLifeCost());
        }
        StackEntry spell = unless.counters() || unless.retargets()
                ? spellOnStack(gameData, ability.targetCardId()) : null;
        if (accepted && !ransom.unpayable()) {
            ResolutionCostPaymentService.Payment payment = new ResolutionCostPaymentService.Payment(
                    ability.sourceCard(), ability.sourceControllerId(), ability, entry);
            ResolutionCostPaymentService.Outcome outcome;
            if (ransom.cost() instanceof AllOfCost allOf) {
                // All or nothing (CR 601.2h, 118.3): every part payable from what they have now, or nothing is paid.
                if (costPayment.canPayNow(gameData, player.getId(), allOf, ability.sourceCard(), entry)) {
                    entry.setUnlessAnswer(state.answered(true));
                    outcome = payParts(gameData, entry, index, player, manaFirst(allOf), payment);
                } else {
                    outcome = ResolutionCostPaymentService.Outcome.NOT_PAID;
                }
            } else {
                outcome = costPayment.pay(gameData, player, ransom.cost(), payment);
                if (outcome == ResolutionCostPaymentService.Outcome.ASKING && ransom.manaOrLife()) {
                    // Lich's Duel Mastery's death replacements are chosen first; this answer is completed again.
                    return false;
                }
                if (outcome != ResolutionCostPaymentService.Outcome.NOT_PAID) {
                    entry.setUnlessAnswer(state.answered(true));
                }
            }
            if (outcome != ResolutionCostPaymentService.Outcome.NOT_PAID) {
                // Paying is taking the action of "do this only once each turn" (CR 603.2h).
                TriggeredAbilityStackService.markActionTaken(gameData, entry);
            }
            if (outcome == ResolutionCostPaymentService.Outcome.ASKING) {
                // Paid by the prompt's answer: the choice to pay is made now, the payment comes with the answer.
                logPaid(gameData, player, "chooses to " + ransom.declined(), unless, spell, ability);
                return false;
            }
            if (outcome == ResolutionCostPaymentService.Outcome.PAID) {
                logPaid(gameData, player, ransom.done(), unless, spell, ability);
                return true;
            }
        }
        if (spell != null && unless.counters() && !counterable(gameData, spell, ability)) {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " declines to " + ransom.declined() + ". ", spell.getCard(),
                    " cannot be countered."));
        } else {
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " declines to " + ransom.declined() + "."));
        }
        log.info("Game {} - {} declines to {} ({})", gameData.id, player.getUsername(), ransom.declined(),
                ability.sourceCard().getName());
        entry.setUnlessAnswer(asked + 1 < alternatives.size() ? state.askingAlternative(asked + 1)
                : state.answered(false));
        return true;
    }

    /** An all-of ransom's parts in payment order: the mana first, then the others as the text lists them. */
    private static List<CostEffect> manaFirst(AllOfCost allOf) {
        return allOf.parts().stream()
                .sorted(Comparator.comparingInt(part -> part instanceof PayManaCost ? 0 : 1))
                .toList();
    }

    /**
     * Pays {@code parts} for {@code payer} in order; a part that asks ({@link ResolutionCostPaymentService.Outcome#ASKING})
     * keeps the rest on the entry's {@link UnlessAnswer}, paid by {@link #nextStep} once its prompt is answered and the
     * resolution comes back to this effect.
     */
    private ResolutionCostPaymentService.Outcome payParts(GameData gameData, StackEntry entry, int index,
                                                          Player payer, List<CostEffect> parts,
                                                          ResolutionCostPaymentService.Payment payment) {
        List<CostEffect> rest = new ArrayList<>(parts);
        while (!rest.isEmpty()) {
            CostEffect part = rest.removeFirst();
            ResolutionCostPaymentService.Outcome outcome = costPayment.pay(gameData, payer, part, payment);
            if (outcome == ResolutionCostPaymentService.Outcome.ASKING) {
                UnlessAnswer state = entry.unlessAnswer(index);
                if (state != null) {
                    entry.setUnlessAnswer(state.paying(payer.getId(), rest));
                }
                return outcome;
            }
        }
        UnlessAnswer state = entry.unlessAnswer(index);
        if (state != null) {
            entry.setUnlessAnswer(state.paying(payer.getId(), List.of()));
        }
        return ResolutionCostPaymentService.Outcome.PAID;
    }

    private void logPaid(GameData gameData, Player player, String done, UnlessPaysEffect unless, StackEntry spell,
                         PendingMayAbility ability) {
        if (spell != null) {
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " " + done + ". ",
                    spell.getCard(), unless.counters() ? " is not countered." : " keeps its target."));
        } else {
            gameLogService.append(gameData, GameLog.text(player.getUsername() + " " + done + "."));
        }
        log.info("Game {} - {} {} ({})", gameData.id, player.getUsername(), done, ability.sourceCard().getName());
    }

    private static StackEntry spellOnStack(GameData gameData, UUID targetableId) {
        if (targetableId == null) {
            return null;
        }
        return gameData.stack.stream()
                .filter(entry -> targetableId.equals(entry.getTargetableId()))
                .findFirst()
                .orElse(null);
    }

    /** Whether the spell the payer declined to pay for can be countered, for the decline's log line. */
    private boolean counterable(GameData gameData, StackEntry spell, PendingMayAbility ability) {
        if (spell.getEntryType().isAbility()) {
            return !gameQueryService.areAbilitiesUncounterable(gameData);
        }
        return !gameQueryService.isUncounterable(gameData, spell.getCard())
                && !gameQueryService.isProtectedFromCounterBySourceCard(
                gameData, spell.getControllerId(), ability.sourceCard());
    }
}
