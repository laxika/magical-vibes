package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardsCost;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.FlipCoinsCost;
import com.github.laxika.magicalvibes.model.effect.GainControlOfPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.OpponentCreatesTokensCost;
import com.github.laxika.magicalvibes.model.effect.OpponentGainsLifeCost;
import com.github.laxika.magicalvibes.model.effect.PutCardsFromSingleGraveyardOnBottomOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnOpponentCreatureCost;
import com.github.laxika.magicalvibes.model.effect.MillControllerCost;
import com.github.laxika.magicalvibes.model.effect.PayEchoCost;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.PutCardsFromGraveyardOnBottomOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.PutTypedCounterOnSourceCost;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardToHandCost;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
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
import com.github.laxika.magicalvibes.model.effect.PayXManaCost;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.LandDropCost;
import com.github.laxika.magicalvibes.model.effect.PutCardFromHandOnTopOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentToHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreaturesWithTotalPowerCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SourceControllerDrawsCost;
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
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
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
 * payer's choice on the resolving entry ({@link StackEntry#setUnlessAnswer}), and the resolution runs this effect again,
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
 * <p>Paid together ({@link UnlessPaysEffect#paidTogether()}): an accepting player's choice is made now
 * ({@link ResolutionCostPaymentService#choose}: the permanents picked, unasked when only that many could be), kept on
 * the {@link UnlessAnswer} with nothing changed while the others decide, and after the last answer every chosen payment
 * is made together, one event, before the branch ({@link #paymentMadeTogether}, CR 101.4, 608.2e). No player receives
 * priority while the players answer and a triggered ability waits to be put on the stack until one would (CR 603.3),
 * so paying as each player accepts and countering after the last answer, as the plain loop does, can't be told apart
 * from countering at the first payment ("as soon as any player performs the action", the PLC punisher rulings).
 *
     * <p>"For each [object] … unless" ({@link UnlessPaysEffect#forEach}): the objects are found once and asked about one
 * at a time in the APNAP order of their payers ({@link UnlessAnswer#remainingObjects}), each by its own payers (its
 * controller or owner, or every player the subject names); an object that left before its turn is skipped. After the
 * last answer the objects nobody paid for are recorded as the entry's {@code CHOOSE} step and {@code ifNotPaid},
 * which acts on them, resolves once (CR 608.2c, 101.4). Cards ({@link UnlessPaysEffect#forEachCard}) the same way,
 * in the subject's order, a card that left the zone it was found in skipped (CR 400.7), the unpaid ones recorded
 * where they are.
 *
 * <p>What can pay: the payer's mana pool and the mana they could still make (they may activate mana abilities while
 * asked, CR 605.3a); mana that can be spent only on {X} for an announced-X ransom; an X chosen while paying
 * ({@link PayXManaCost}), asked as one number (0 is not paying) and made the ability's X (CR 107.3f); 2 life for each {B} under K'rrik,
 * Son of Yawgmoth; life only while the payer can lose life (CR 119.4, 119.8); the other costs as
 * {@link ResolutionCostPaymentService} judges and pays them, through the shared actions. An amount read from the source
 * reads the source on the battlefield, else as it last existed (CR 608.2h). An echo cost ({@link PayEchoCost}) is
 * asked and paid as the cost it names ({@link #alternatives}).
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
            return ofManaAndLife(manaCost, xCost, life, false);
        }

        /**
         * {@link #ofManaAndLife(String, boolean, int)} for a cumulative upkeep when {@code cumulativeUpkeep}: mana spendable
         * only on cumulative upkeep costs may pay it (Snowfall, Adarkar Unicorn).
         */
        static Ransom ofManaAndLife(String manaCost, boolean xCost, int life, boolean cumulativeUpkeep) {
            String text = manaCost == null ? life + " life" : life > 0 ? manaCost + " and " + life + " life" : manaCost;
            CostEffect cost = manaCost == null ? new PayLifeCost(life)
                    : new PayManaCost(manaCost, null, null, cumulativeUpkeep, life, false, xCost);
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

        /** Whether mana spendable only on cumulative upkeep costs may pay it. */
        public boolean cumulativeUpkeep() {
            return cost instanceof PayManaCost mana && mana.forCumulativeUpkeep();
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
        /**
         * Nothing happens: the spell the effect is about left the stack, or a payment was made for every object of
         * "for each [object] … unless".
         */
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
        List<Ransom> alternatives = alternatives(gameData, entry, unless, payerId);
        int asked = Math.min(state.alternative(), alternatives.size() - 1);
        Ransom ransom = alternatives.get(asked);
        if (ransom.cost() instanceof PayXManaCost xCost) {
            // The X is chosen while paying (CR 107.3f): one number, 0 not paying; mana abilities may be activated
            // meanwhile (CR 605.3a). The answer resumes this resolution, which reads it in nextStep.
            String source = FaceDownVisibility.sourceName(gameData, entry);
            costPayment.askX(gameData, payerId, xCost, unless.prompt() != null ? unless.prompt()
                    : "You may pay " + xCost.manaCost() + " for " + source + ". Choose X (0 = don't pay).", source);
            return;
        }
        StackEntry spell = spellAskedAbout(gameData, entry, unless);
        UUID object = state.currentObject();
        String prompt = unless.prompt() != null && alternatives.size() == 1 ? unless.prompt()
                : ransom.action() + suffix(gameData, entry, unless, spell, payerId) + objectSuffix(gameData, entry, unless, object)
                + "?";
        gameData.resolvingMayEffectFromStack = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), payerId, List.of(unless), prompt,
                object != null ? object : spell == null ? entry.getTargetId() : spell.getTargetableId(),
                ransom.manaCost(),
                entry.getSourcePermanentId(), 0, ransom.life(), null, null, null,
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
        if (state != null && state.currentPayer() != null && gameData.chosenXValue != null
                && unless.ransom() instanceof PayXManaCost) {
            Step asked = answerX(gameData, entry, unless, state);
            if (asked != null) {
                return asked;
            }
            state = entry.unlessAnswer(index);
        }
        if (state == null && unless.asksForEachObject()) {
            List<UUID> objects = objectsToAskAbout(gameData, entry, unless);
            state = UnlessAnswer.askingAbout(index, objects,
                    objects.isEmpty() ? List.of() : objectPayers(gameData, entry, unless, objects.getFirst()));
        }
        if (state == null) {
            List<UUID> payers = payers(gameData, entry, unless);
            if (payers == null) {
                return Step.NOTHING;
            }
            state = UnlessAnswer.asking(index, payers);
        }
        boolean actionTaken = onceEachTurnActionTaken(gameData, entry);
        while (true) {
            while (state.currentPayer() != null) {
                UUID head = state.currentPayer();
                int payable = actionTaken || !gameData.playerIds.contains(head) ? -1
                        : firstPayable(gameData, entry, head, alternatives(gameData, entry, unless, head),
                        state.alternative());
                if (payable >= 0) {
                    state = state.askingAlternative(payable);
                    break;
                }
                state = state.answered(false);
            }
            if (state.currentPayer() != null || state.currentObject() == null) {
                break;
            }
            // Every payer answered for this object: on to the next one still on the battlefield.
            state = nextObject(gameData, entry, unless, state, false);
            while (state.currentObject() != null && objectGone(gameData, entry, unless, state.currentObject())) {
                state = nextObject(gameData, entry, unless, state, true);
            }
            if (state.currentObject() == null) {
                break;
            }
        }
        if (state.currentPayer() == null && unless.asksForEachObject()) {
            // The branch acts once on every object nobody paid for, together (CR 608.2c, 101.4).
            List<UUID> unpaid = state.unpaidObjectIds();
            entry.recordStep(StepKind.CHOOSE, unpaid.isEmpty() ? StepOutcome.NONE
                    : unless.forEachCard() != null ? unpaidCards(gameData, entry, unless, unpaid)
                    : StepOutcome.ofObjects(unpaid, unpaid.stream()
                            .map(id -> subjectResolutionService.controllerOf(gameData, entry, unless, id))
                            .filter(java.util.Objects::nonNull).distinct().toList()));
            entry.clearUnlessAnswer(index);
            return unpaid.isEmpty() ? Step.NOTHING : Step.NOT_PAID;
        }
        if (state.currentPayer() == null) {
            // "Those who didn't pay" (eachUnless) reads the payers who chose to pay (CR 101.4). No other branch reads
            // them, so another "unless" keeps the accepted players of an earlier "each player may" choice of the entry.
            if (unless.perPayer()) {
                entry.setAcceptedPlayerIds(state.paidPlayerIds());
            }
            if (unless.paidTogether() && state.anyPaid()) {
                // The choices wait on the entry for paymentMadeTogether, which the resolution asks next.
                entry.setUnlessAnswer(state);
                return Step.PAID;
            }
            entry.clearUnlessAnswer(index);
            return state.anyPaid() ? Step.PAID : Step.NOT_PAID;
        }
        entry.setUnlessAnswer(state);
        return Step.ASK;
    }

    /**
     * The payer's answer to an X chosen while paying ({@link PayXManaCost}, asked by {@link #resolve}): 0 is not paying;
     * an X above its limit or one their pool can't pay now is asked again, the payer having been able to tap for it
     * while asked (CR 605.3a, 118.3); otherwise the cost with that X is paid through the resolution-time payment (the
     * {@code PAY_MANA} step, K'rrik's life, Powerstone mana: not a spell) and that X becomes the ability's X (CR 107.3f),
     * read by the branch and by a reflexive "when you do" it makes (CR 603.12). Paying {B} with life is losing it
     * (CR 119.4): a Lich's Duel Mastery sacrifice it would cause has its death replacements chosen first (CR 616.1),
     * and the X answer is then completed again.
     *
     * @return {@link Step#ASK} when the payer is asked again, {@link Step#PAYING} while a death replacement is chosen
     *         first, {@code null} when the answer is recorded
     */
    private Step answerX(GameData gameData, StackEntry entry, UnlessPaysEffect unless, UnlessAnswer state) {
        int x = gameData.chosenXValue;
        gameData.chosenXValue = null;
        UUID payerId = state.currentPayer();
        String payerName = gameData.playerIdToName.get(payerId);
        Ransom ransom = alternatives(gameData, entry, unless, payerId).getFirst();
        PayXManaCost xCost = (PayXManaCost) ransom.cost();
        if (x <= 0) {
            gameLogService.append(gameData, GameLog.text(payerName + " declines to " + ransom.declined() + "."));
            entry.setUnlessAnswer(state.answered(false));
            return null;
        }
        PayManaCost paid = ResolutionCostPaymentService.paidWith(xCost, x);
        if (x > ResolutionCostPaymentService.maximumX(xCost)
                || !costPayment.canPayNow(gameData, payerId, paid, entry.getCard(), entry)) {
            gameLogService.append(gameData, GameLog.text(payerName + " can't pay " + xCost.withX(x)
                    + " (tap mana sources, then choose X again)."));
            return Step.ASK;
        }
        Player payer = new Player(payerId, payerName);
        GameData.InteractionAnswerReplay replay = new GameData.InteractionAnswerReplay(payer,
                new PendingInteraction.XValueChoice(payerId, 0, x, "", FaceDownVisibility.sourceName(gameData, entry),
                        true, xCost.manaCost()), new InteractionAnswer.NumberChosen(x));
        if (costPayment.pay(gameData, payer, paid, new ResolutionCostPaymentService.Payment(entry.getCard(),
                entry.getControllerId(), null, entry, replay)) == ResolutionCostPaymentService.Outcome.ASKING) {
            return Step.PAYING;
        }
        // The text defines no other X for the ability, so this one replaces what the entry kept there (an attack
        // trigger keeps the number of attackers).
        entry.setXValue(x);
        // Paying is taking the action of "do this only once each turn" (CR 603.2h).
        TriggeredAbilityStackService.markActionTaken(gameData, entry);
        gameLogService.append(gameData, GameLog.text(payerName + " pays " + xCost.withX(x) + "."));
        log.info("Game {} - {} pays {} ({})", gameData.id, payerName, xCost.withX(x), entry.getCard().getName());
        entry.setUnlessAnswer(state.answered(true));
        return null;
    }

    /**
     * The objects "for each [object] … unless" asks about, found once (CR 608.2h): those of {@code unless.forEach()} on
     * the battlefield, in the APNAP order of the player who answers for them (CR 101.4): their controller, or their
     * owner for "its owner", in the order the subject names them within one player's.
     */
    private List<UUID> objectsToAskAbout(GameData gameData, StackEntry entry, UnlessPaysEffect unless) {
        if (unless.forEachCard() != null) {
            // The cards in the order the subject names them; every one is asked about by the same payers.
            return cardsToAskAbout(gameData, entry, unless).stream().map(located -> located.card().getId())
                    .distinct().toList();
        }
        List<UUID> apnap = subjectResolutionService.players(gameData, entry, unless, Subjects.eachPlayer());
        List<UUID> objects = new ArrayList<>(subjectResolutionService.permanentIds(gameData, entry, unless,
                unless.forEach()).stream()
                .distinct()
                .filter(id -> gameQueryService.findPermanentById(gameData, id) != null)
                .toList());
        boolean byOwner = unless.payer() instanceof PlayerSubject.OwnerOf && unless.payerReadPerObject();
        objects.sort(Comparator.comparingInt(id -> {
            UUID player = byOwner ? subjectResolutionService.ownerOf(gameData, entry, unless, id)
                    : subjectResolutionService.controllerOf(gameData, entry, unless, id);
            int place = apnap.indexOf(player);
            return place < 0 ? Integer.MAX_VALUE : place;
        }));
        return objects;
    }

    /** The cards of {@code unless.forEachCard()} where it names them now (CR 400.7: a card that moved isn't named). */
    private List<SubjectResolutionService.LocatedCard> cardsToAskAbout(GameData gameData, StackEntry entry,
                                                                     UnlessPaysEffect unless) {
        return subjectResolutionService.locatedCards(gameData, entry, unless, unless.forEachCard());
    }

    /**
     * Whether the object asked about left before its turn came: a permanent no longer on the battlefield, a card no
     * longer where the subject found it (CR 400.7); nothing is done to it (CR 608.2h).
     */
    private boolean objectGone(GameData gameData, StackEntry entry, UnlessPaysEffect unless, UUID objectId) {
        if (unless.forEachCard() != null) {
            return cardsToAskAbout(gameData, entry, unless).stream()
                    .noneMatch(located -> located.card().getId().equals(objectId));
        }
        return gameQueryService.findPermanentById(gameData, objectId) == null;
    }

    /**
     * The unpaid cards of "for each [card] … unless" as a {@code CHOOSE} step: each where it is, bound to that stay,
     * so {@code cardsThisWay(CHOOSE)} names it there (as {@code ChooseCardEffect} records its cards).
     */
    private StepOutcome unpaidCards(GameData gameData, StackEntry entry, UnlessPaysEffect unless, List<UUID> unpaid) {
        List<UUID> cardIds = new ArrayList<>();
        List<UUID> ownerIds = new ArrayList<>();
        List<com.github.laxika.magicalvibes.model.MovedCard> moved = new ArrayList<>();
        for (SubjectResolutionService.LocatedCard located : cardsToAskAbout(gameData, entry, unless)) {
            UUID cardId = located.card().getId();
            if (!unpaid.contains(cardId) || cardIds.contains(cardId)) {
                continue;
            }
            cardIds.add(cardId);
            if (located.ownerId() != null && !ownerIds.contains(located.ownerId())) {
                ownerIds.add(located.ownerId());
            }
            moved.add(gameData.boundToStay(new com.github.laxika.magicalvibes.model.MovedCard(cardId, cardId,
                    located.zone(), located.ownerId())));
        }
        return cardIds.isEmpty() ? StepOutcome.NONE : StepOutcome.ofObjects(cardIds, ownerIds, moved);
    }

    /**
     * The players asked to pay for {@code objectId}: its controller or owner as it is now ({@code controllerOf} /
     * {@code ownerOf} the objects, CR 608.2h), else every player the payer subject names, in APNAP order.
     */
    private List<UUID> objectPayers(GameData gameData, StackEntry entry, UnlessPaysEffect unless, UUID objectId) {
        if (!unless.payerReadPerObject()) {
            return subjectResolutionService.players(gameData, entry, unless, unless.payer());
        }
        UUID player = unless.payer() instanceof PlayerSubject.OwnerOf
                ? subjectResolutionService.ownerOf(gameData, entry, unless, objectId)
                : subjectResolutionService.controllerOf(gameData, entry, unless, objectId);
        return player == null ? List.of() : List.of(player);
    }

    /**
     * {@code state} after every payer answered for its object ({@code skipped}: the object was gone): the next object
     * is asked about, by its own payers.
     */
    private UnlessAnswer nextObject(GameData gameData, StackEntry entry, UnlessPaysEffect unless, UnlessAnswer state,
                                    boolean skipped) {
        List<UUID> objects = state.remainingObjects();
        UUID next = objects.size() > 1 ? objects.get(1) : null;
        return state.nextObject(skipped, next == null || objectGone(gameData, entry, unless, next)
                ? List.of() : objectPayers(gameData, entry, unless, next));
    }

    /**
     * " for Lantern Kami": the object "for each [object] … unless" asks about, by the name every player may read; a
     * card by its name, as the cards it asks about are revealed ({@link UnlessPaysEffect#forEachCard}).
     */
    private String objectSuffix(GameData gameData, StackEntry entry, UnlessPaysEffect unless, UUID objectId) {
        if (objectId == null) {
            return "";
        }
        if (unless.forEachCard() != null) {
            return cardsToAskAbout(gameData, entry, unless).stream()
                    .filter(located -> located.card().getId().equals(objectId))
                    .findFirst()
                    .map(located -> " for " + located.card().getName())
                    .orElse("");
        }
        Permanent object = gameQueryService.findPermanentById(gameData, objectId);
        return object == null ? "" : " for " + FaceDownVisibility.publicName(gameData, object);
    }

    /**
     * The steps paying {@code unless}'s ransom for every player who chose to pay it, once {@link #nextStep} found every
     * payer answered with one paying ({@link Step#PAID}), to be resolved before the branch: none unless it is paid
     * together ({@link UnlessPaysEffect#paidTogether()}), which each answer only chose
     * ({@link ResolutionCostPaymentService#payTogether}). The choices are taken off the entry.
     */
    public List<CardEffect> paymentMadeTogether(GameData gameData, StackEntry entry, UnlessPaysEffect unless,
                                                int index) {
        UnlessAnswer state = entry.unlessAnswer(index);
        if (!unless.paidTogether() || state == null) {
            return List.of();
        }
        entry.clearUnlessAnswer(index);
        return costPayment.payTogether(gameData, entry, ransom(gameData, entry, unless).cost(), state.choices());
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
        return payers.stream().noneMatch(payerId -> firstPayable(gameData, entry, payerId,
                alternatives(gameData, entry, unless, payerId), 0) >= 0);
    }

    /**
     * Whether a player already chose to pay the "unless" payment the waiting resolution asks about: another payment by
     * the next player asked changes nothing (an AI declines it). Never for a branch decided by each payer's own choice
     * ({@link UnlessPaysEffect#perPayer()}).
     */
    public static boolean anotherPlayerAlreadyPaid(GameData gameData) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        int index = gameData.pendingEffectResolutionIndex;
        if (entry == null || index < 0 || index >= entry.getEffectsToResolve().size()
                || !(entry.getEffectsToResolve().get(index) instanceof UnlessPaysEffect)) {
            return false;
        }
        UnlessAnswer state = entry.unlessAnswer(index);
        // "Each of them … unless they pay": a payment pays only for its payer (eachUnless).
        return state != null && state.anyPaid()
                && !((UnlessPaysEffect) entry.getEffectsToResolve().get(index)).perPayer();
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
                .filter(stackEntry -> triggeringCardId != null && stackEntry.isIdentifiedBy(triggeringCardId))
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

    /**
     * {@code unless}'s ransom with its amounts read as it resolves; an any-of ransom's alternatives in order. A life
     * payment is read for the effect's controller ({@link #alternatives(GameData, StackEntry, UnlessPaysEffect, UUID)}
     * reads it for another payer).
     */
    public List<Ransom> alternatives(GameData gameData, StackEntry entry, UnlessPaysEffect unless) {
        return alternatives(gameData, entry, unless, entry.getControllerId());
    }

    /**
     * {@code unless}'s ransom as {@code payerId} would pay it: a life amount is read against the payer ("half their
     * life, rounded up": Temporal Extortion), every other amount against the resolving entry.
     */
    public List<Ransom> alternatives(GameData gameData, StackEntry entry, UnlessPaysEffect unless, UUID payerId) {
        CostEffect asked = unless.ransom() instanceof PayEchoCost echo ? echoCost(gameData, entry, echo)
                : unless.ransom();
        if (asked instanceof AnyOfCost anyOf) {
            return anyOf.alternatives().stream().map(cost -> ransom(gameData, entry, cost, payerId)).toList();
        }
        return List.of(ransom(gameData, entry, asked, payerId));
    }

    /**
     * Echo's cost as it is paid now (CR 702.30a): the cost it names, an {X} read as the ability resolves (Volcano
     * Hellion's ruling: the life total then). Under "you may pay {0} rather than pay the echo cost for permanents you
     * control" (Thick-Skinned Goblin) the payer may pay that, or the echo cost, or neither (its ruling): the
     * alternative first, then the echo cost.
     */
    private CostEffect echoCost(GameData gameData, StackEntry entry, PayEchoCost echo) {
        CostEffect printed = echo.handCardCost() != null ? echo.handCardCost()
                : echo.cost() != null ? echo.cost()
                : echo.dynamicEchoCost() != null ? PayManaCost.withGenericIncrease("{0}", echo.dynamicEchoCost())
                : new PayManaCost(echo.echoCost());
        String alternative = gameQueryService.findAlternativeEchoCost(gameData, entry.getControllerId());
        if (alternative == null
                || printed instanceof PayManaCost mana && mana.genericIncrease() == null
                && alternative.equals(mana.manaCost())) {
            return printed;
        }
        return AnyOfCost.of(new PayManaCost(alternative), printed);
    }

    /** {@code unless}'s ransom with its amounts read as it resolves (the first alternative of an any-of ransom). */
    public Ransom ransom(GameData gameData, StackEntry entry, UnlessPaysEffect unless) {
        return alternatives(gameData, entry, unless).getFirst();
    }

    private Ransom ransom(GameData gameData, StackEntry entry, CostEffect cost, UUID payerId) {
        return switch (cost) {
            case PayManaCost mana when mana.useTriggeringSpellManaCost() -> {
                String manaCost = triggeringSpellManaCost(gameData, entry);
                yield manaCost == null ? Ransom.unpayableMana() : Ransom.ofManaAndLife(manaCost, false, 0);
            }
            case PayManaCost mana when mana.useSourceManaCost() -> {
                String manaCost = sourceManaCost(gameData, entry);
                yield manaCost == null ? Ransom.unpayableMana() : Ransom.ofManaAndLife(manaCost, false, 0);
            }
            case PayManaCost mana -> Ransom.ofManaAndLife(manaCost(gameData, entry, mana), mana.announcedX(),
                    mana.lifeAmount(), mana.forCumulativeUpkeep());
            case PayXManaCost xCost -> new Ransom(xCost.maximumX() == null ? xCost
                    : new PayXManaCost(xCost.manaCost(), new Fixed(Math.max(0, amountEvaluationService.evaluate(
                            gameData, xCost.maximumX(), amountContext(gameData, entry))))),
                    "Pay " + xCost.manaCost(), "pays " + xCost.manaCost(), "pay " + xCost.manaCost());
            case PayLifeCost life -> Ransom.ofManaAndLife(null, false, Math.max(0,
                    amountEvaluationService.evaluate(gameData, life.amount(),
                            amountContext(gameData, entry).withControllerId(payerId))));
            case DiscardCardTypeCost discard -> {
                String kind = discard.label() == null ? "" : discard.label() + " ";
                String card = discard.count() > 1 ? countWord(discard.count()) + " " + kind + "cards"
                        : discard.label() == null ? "a card" : article(discard.label()) + " card";
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
                String what = sacrifice.count() == 1 && sacrifice.description() != null
                        ? article(sacrifice.description())
                        : countWord(sacrifice.count()) + " "
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
                String what = (count == 1 ? tap.excludeSource() ? "another " : "an " : countWord(count) + " ")
                        + "untapped "
                        + (tap.description() != null ? tap.description() : count == 1 ? "permanent" : "permanents");
                yield new Ransom(cost, "Tap " + what + " you control", "taps " + what,
                        "tap " + what);
            }
            case ExileCardsCost ignored -> new Ransom(cost, "Exile that card", "exiles that card",
                    "exile that card");
            case SourceControllerDrawsCost draw -> {
                String drawer = gameData.playerIdToName.get(entry.getControllerId());
                String cards = draw.count() == 1 ? "a card" : draw.count() + " cards";
                yield new Ransom(cost, "Have " + drawer + " draw " + cards, "has " + drawer + " draw " + cards,
                        "have " + drawer + " draw " + cards);
            }
            case ReturnPermanentToHandCost toHand when toHand.count() > 1 -> {
                String what = countWord(toHand.count()) + " "
                        + (toHand.description() == null ? "permanents you control" : toHand.description());
                yield new Ransom(cost, "Return " + what + " to their owner's hand",
                        "returns " + what + " to their owner's hand", "return " + what + " to their owner's hand");
            }
            case ReturnPermanentToHandCost toHand -> {
                String what = article(toHand.description() == null ? "permanent you control" : toHand.description());
                yield new Ransom(cost, "Return " + what + " to its owner's hand",
                        "returns " + what + " to its owner's hand", "return " + what + " to its owner's hand");
            }
            case PayEnergyCost energy -> {
                int amount = Math.max(0, amountEvaluationService.evaluate(gameData, energy.amount(),
                        amountContext(gameData, entry)));
                String text = amount == 0 ? "0 {E}" : "{E}".repeat(amount);
                yield new Ransom(new PayEnergyCost(amount), "Pay " + text, "pays " + text, "pay " + text);
            }
            case ExileTopCardOfGraveyardCost top -> {
                String what = "the top " + (top.requiredType() == null ? ""
                        : top.requiredType().name().toLowerCase() + " ") + "card of ";
                yield new Ransom(cost, "Exile " + what + "your graveyard", "exiles " + what + "their graveyard",
                        "exile " + what + "their graveyard");
            }
            case RemoveCounterFromSourceCost counters -> {
                String what = counters(counters.count(), counters.counterType()) + " from "
                        + FaceDownVisibility.sourceName(gameData, entry);
                yield new Ransom(cost, "Remove " + what, "removes " + what, "remove " + what);
            }
            case RemoveCounterFromControlledPermanentCost counters -> {
                CounterType kind = counters.allowedCounterTypes().size() == 1
                        ? counters.allowedCounterTypes().getFirst() : CounterType.ANY;
                String what = counters(counters.count(), kind) + (counters.count() == 1
                        ? " from a permanent you control" : " from among permanents you control");
                yield new Ransom(cost, "Remove " + what, "removes " + what, "remove " + what);
            }
            case PutCardsFromGraveyardOnBottomOfLibraryCost bottom -> {
                String cards = bottom.count() == 1 ? "a card" : countWord(bottom.count()) + " cards";
                yield new Ransom(cost, "Put " + cards + " from your graveyard on the bottom of your library",
                        "puts " + cards + " from their graveyard on the bottom of their library",
                        "put " + cards + " from their graveyard on the bottom of their library");
            }
            case ReturnCardFromGraveyardToHandCost toHand -> {
                String card = article(CardPredicateUtils.describeFilter(toHand.predicate()));
                yield new Ransom(cost, "Return " + card + " from your graveyard to your hand",
                        "returns " + card + " from their graveyard to their hand",
                        "return " + card + " from their graveyard to their hand");
            }
            case MillControllerCost mill -> {
                String cards = mill.count() == 1 ? "a card" : countWord(mill.count()) + " cards";
                yield new Ransom(cost, "Mill " + cards, "mills " + cards, "mill " + cards);
            }
            case PutTypedCounterOnSourceCost counter -> {
                String what = counters(counter.count(), counter.counterType()) + " on "
                        + FaceDownVisibility.sourceName(gameData, entry);
                yield new Ransom(cost, "Put " + what, "puts " + what, "put " + what);
            }
            case PutCardFromHandOnTopOfLibraryCost ignored -> new Ransom(cost,
                    "Put a card from your hand on top of your library",
                    "puts a card from their hand on top of their library",
                    "put a card from their hand on top of their library");
            case LandDropCost ignored -> new Ransom(cost, "Pay {D}",
                    "pays {D}", "pay {D}");
            case SacrificeCreaturesWithTotalPowerCost power -> {
                String what = "creatures with total power " + power.totalPower() + " or greater";
                yield new Ransom(cost, "Sacrifice " + what, "sacrifices " + what, "sacrifice " + what);
            }
            case FlipCoinsCost flip -> {
                String coins = flip.count() == 1 ? "a coin" : countWord(flip.count()) + " coins";
                yield new Ransom(cost, "Flip " + coins, "flips " + coins, "flip " + coins);
            }
            case OpponentCreatesTokensCost tokens -> {
                String what = tokens.count() == 1 ? article(tokens.tokenTemplate().tokenName() + " token")
                        : countWord(tokens.count()) + " " + tokens.tokenTemplate().tokenName() + " tokens";
                yield new Ransom(cost, "Have an opponent create " + what, "has an opponent create " + what,
                        "have an opponent create " + what);
            }
            case PutCounterOnOpponentCreatureCost counters -> {
                String what = counters.count() == 1
                        ? counters(1, counters.counterType()) + " on a creature an opponent controls"
                        : counters(counters.count(), counters.counterType())
                                + " on creatures an opponent controls, one at a time";
                yield new Ransom(cost, "Put " + what, "puts " + what, "put " + what);
            }
            case DrawCardsCost draw -> {
                String cards = draw.count() == 1 ? "a card" : countWord(draw.count()) + " cards";
                yield new Ransom(cost, "Draw " + cards, "draws " + cards, "draw " + cards);
            }
            case ExileTopCardOfLibraryCost exile -> {
                String what = exile.count() == 1 ? "the top card" : "the top " + countWord(exile.count()) + " cards";
                yield new Ransom(cost, "Exile " + what + " of your library", "exiles " + what + " of their library",
                        "exile " + what + " of their library");
            }
            case PutCardsFromSingleGraveyardOnBottomOfLibraryCost bottom -> {
                String what = countWord(bottom.cardsPerPayment()) + " cards from a single graveyard"
                        + (bottom.payments() == 1 ? "" : ", " + countWord(bottom.payments()) + " times,");
                yield new Ransom(cost, "Put " + what + " on the bottom of their owner's library",
                        "puts " + what + " on the bottom of their owner's library",
                        "put " + what + " on the bottom of their owner's library");
            }
            case OpponentGainsLifeCost life -> new Ransom(cost, "Have an opponent gain " + life.amount() + " life",
                    "has an opponent gain " + life.amount() + " life", "have an opponent gain " + life.amount()
                            + " life");
            case GainControlOfPermanentsCost control -> {
                String what = control.count() == 1 ? "a permanent" : countWord(control.count()) + " permanents";
                yield new Ransom(cost, "Gain control of " + what + " you don't control",
                        "gains control of " + what, "gain control of " + what);
            }
            case AllOfCost allOf -> {
                List<Ransom> parts = allOf.parts().stream().map(part -> ransom(gameData, entry, part, payerId))
                        .toList();
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

    /**
     * "Its mana cost" of the source ("sacrifice this artifact unless you pay its mana cost"): the source permanent's, on
     * the battlefield or as it last existed (CR 608.2h), an {X} in it being 0; {@code null} when it can't be paid: a
     * face-down permanent has no mana cost (CR 708.2), and no mana cost can't be paid (CR 118.6).
     */
    private String sourceManaCost(GameData gameData, StackEntry entry) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        if (source != null && source.isFaceDown()) {
            return null;
        }
        String manaCost = source != null ? source.getCard().getManaCost() : entry.getCard().getManaCost();
        if (manaCost == null || manaCost.isEmpty()) {
            return null;
        }
        return manaCost.replace("{X}", "{0}");
    }

    /** "two", "three" … for the small counts a ransom names, else the digits. */
    private static String countWord(int count) {
        return switch (count) {
            case 2 -> "two";
            case 3 -> "three";
            case 4 -> "four";
            case 5 -> "five";
            default -> String.valueOf(count);
        };
    }

    /** "a +1/+1 counter", "two oil counters", "a counter" (any kind). */
    private static String counters(int count, CounterType kind) {
        String name = switch (kind) {
            case ANY -> "";
            case PLUS_ONE_PLUS_ONE -> "+1/+1 ";
            case MINUS_ONE_MINUS_ONE -> "-1/-1 ";
            default -> kind.name().toLowerCase().replace('_', ' ') + " ";
        };
        if (count == 1) {
            return article(name + "counter");
        }
        return countWord(count) + " " + name + "counters";
    }

    /** "a creature", "an Island"; a description that already reads with its determiner ("another creature") as it is. */
    private static String article(String noun) {
        if (noun.startsWith("another ") || noun.startsWith("a ") || noun.startsWith("an ")) {
            return noun;
        }
        return ("aeiou".indexOf(Character.toLowerCase(noun.charAt(0))) >= 0 ? "an " : "a ") + noun;
    }

    private String manaCost(GameData gameData, StackEntry entry, PayManaCost mana) {
        if (mana.timesPaid() != null) {
            int times = amountEvaluationService.evaluate(gameData, mana.timesPaid(), amountContext(gameData, entry));
            return times <= 0 ? "{0}" : mana.manaCost().repeat(times);
        }
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
        List<Ransom> alternatives = alternatives(gameData, entry, unless, player.getId());
        int asked = Math.min(state.alternative(), alternatives.size() - 1);
        Ransom ransom = alternatives.get(asked);
        if (ransom.manaOrLife()) {
            // The mana and life as the question carried them: the payer may have tapped for it while asked.
            ransom = Ransom.ofManaAndLife(ability.manaCost(), ransom.xCost(), ability.additionalLifeCost(),
                    ransom.cumulativeUpkeep());
        }
        StackEntry spell = unless.counters() || unless.retargets()
                ? spellOnStack(gameData, ability.targetCardId()) : null;
        if (accepted && !ransom.unpayable()) {
            ResolutionCostPaymentService.Payment payment = new ResolutionCostPaymentService.Payment(
                    ability.sourceCard(), ability.sourceControllerId(), ability, entry);
            ResolutionCostPaymentService.Outcome outcome;
            if (unless.paidTogether()) {
                // Chosen now, paid with the others' choices after the last answer (CR 101.4).
                ResolutionCostPaymentService.Choice choice = costPayment.choose(gameData, player, ransom.cost(),
                        payment);
                outcome = choice.outcome();
                if (outcome == ResolutionCostPaymentService.Outcome.PAID) {
                    entry.setUnlessAnswer(state.answered(true).withChoice(player.getId(), choice.objectIds()));
                } else if (outcome == ResolutionCostPaymentService.Outcome.ASKING) {
                    entry.setUnlessAnswer(state.answered(true));
                }
                if (outcome != ResolutionCostPaymentService.Outcome.NOT_PAID) {
                    TriggeredAbilityStackService.markActionTaken(gameData, entry);
                    logPaid(gameData, player, "chooses to " + ransom.declined(), unless, spell, ability);
                    return outcome == ResolutionCostPaymentService.Outcome.PAID;
                }
            } else if (ransom.cost() instanceof AllOfCost allOf) {
                // All or nothing (CR 601.2h, 118.3): every part payable from what they have now, or nothing is paid.
                if (costPayment.canPayNow(gameData, player.getId(), allOf, ability.sourceCard(), entry)) {
                    entry.setUnlessAnswer(state.answered(true));
                    outcome = payParts(gameData, entry, index, player, manaFirst(allOf), payment);
                } else {
                    outcome = ResolutionCostPaymentService.Outcome.NOT_PAID;
                }
            } else {
                outcome = costPayment.pay(gameData, player, ransom.cost(), payment);
                if (outcome == ResolutionCostPaymentService.Outcome.ASKING
                        && (ransom.manaOrLife() || ransom.cost() instanceof FlipCoinsCost)) {
                    // Lich's Duel Mastery's death replacements, or Krark's Thumb's kept results, are chosen first;
                    // this answer is completed again.
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
                .filter(entry -> entry.isIdentifiedBy(targetableId))
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
