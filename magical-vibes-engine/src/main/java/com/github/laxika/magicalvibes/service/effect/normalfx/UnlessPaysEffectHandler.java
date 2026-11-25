package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AnyOfCost;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
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
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
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
 * the payer couldn't pay; declining one asks the next, and declining every one is not paying.
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
    public record Ransom(CostEffect cost, String action, String done, String declined) {

        /** A mana and / or life ransom: {@code manaCost} ({@code null} for none), X-only mana, and the life. */
        static Ransom ofManaAndLife(String manaCost, boolean xCost, int life) {
            String text = manaCost == null ? life + " life" : life > 0 ? manaCost + " and " + life + " life" : manaCost;
            CostEffect cost = manaCost == null ? new PayLifeCost(life)
                    : new PayManaCost(manaCost, null, null, false, life, false, xCost);
            return new Ransom(cost, "Pay " + text, "pays " + text, "pay " + text);
        }

        /** The mana to pay, or {@code null} when the ransom isn't (or has no) mana. */
        public String manaCost() {
            return cost instanceof PayManaCost mana && mana.manaCost() != null && !mana.manaCost().isEmpty()
                    ? mana.manaCost() : null;
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

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UnlessPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var unless = (UnlessPaysEffect) effect;
        UUID payerId = payer(gameData, entry, unless);
        if (payerId == null || onceEachTurnActionTaken(gameData, entry)) {
            return;
        }
        int index = entry.getResolvingEffectIndex();
        List<Ransom> alternatives = alternatives(gameData, entry, unless);
        int asked = firstPayable(gameData, payerId, alternatives, entry.unlessAlternative(index));
        if (asked < 0) {
            return;
        }
        if (alternatives.size() > 1) {
            entry.askUnlessAlternative(index, asked);
        }
        Ransom ransom = alternatives.get(asked);
        StackEntry spell = spellAskedAbout(gameData, entry, unless);
        String prompt = unless.prompt() != null && alternatives.size() == 1 ? unless.prompt()
                : ransom.action() + suffix(gameData, unless, spell, payerId) + "?";
        gameData.resolvingMayEffectFromStack = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), payerId, List.of(unless), prompt,
                spell == null ? entry.getTargetId() : spell.getTargetableId(), ransom.manaCost(),
                entry.getSourcePermanentId(), null, 0, ransom.life(), null, null, null,
                entry.getSourcePermanentSnapshot(), entry.getControllerId()));
    }

    /**
     * Whether {@code unless}'s payer could not pay its ransom in full now, so is not asked and its {@code ifNotPaid}
     * happens (CR 118.3, 608.2d): for an any-of ransom, none of the alternatives not yet declined. A payer the subject
     * no longer names (the spell left the stack) is asked nothing and nothing happens, which {@link #resolve} decides.
     */
    public boolean cannotBePaid(GameData gameData, StackEntry entry, UnlessPaysEffect unless, int index) {
        UUID payerId = payer(gameData, entry, unless);
        return payerId != null && (onceEachTurnActionTaken(gameData, entry)
                || firstPayable(gameData, payerId, alternatives(gameData, entry, unless),
                entry.unlessAlternative(index)) < 0);
    }

    /** Whether the entry's "do this only once each turn" action was already taken this turn (CR 603.2h). */
    private static boolean onceEachTurnActionTaken(GameData gameData, StackEntry entry) {
        return entry.marksOnceEachTurnAction() && TriggeredAbilityStackService.actionTaken(gameData, entry);
    }

    /** {@link #cannotBePaid(GameData, StackEntry, UnlessPaysEffect, int)} for the effect the entry is resolving. */
    public boolean cannotBePaid(GameData gameData, StackEntry entry, UnlessPaysEffect unless) {
        return cannotBePaid(gameData, entry, unless, entry.getResolvingEffectIndex());
    }

    /**
     * The player who may pay, or {@code null}: none when the spell or ability the branch is about is no longer on the
     * stack (an earlier instruction countered it "instead", Bring the Ending), as there is nothing left to pay for.
     */
    private UUID payer(GameData gameData, StackEntry entry, UnlessPaysEffect unless) {
        if ((unless.counters() || unless.retargets()) && spellAskedAbout(gameData, entry, unless) == null) {
            return null;
        }
        return subjectResolutionService.player(gameData, entry, unless, unless.payer());
    }

    /** The spell or ability the counter or the retarget of {@code unless} acts on, while it is on the stack. */
    private StackEntry spellAskedAbout(GameData gameData, StackEntry entry, UnlessPaysEffect unless) {
        PermanentSubject spell = unless.counters() ? unless.counteredSpell()
                : unless.retargets() ? Subjects.targetSpell() : null;
        return spell == null ? null : subjectResolutionService.spellOnStack(gameData, entry, spell);
    }

    private static String suffix(GameData gameData, UnlessPaysEffect unless, StackEntry spell, UUID viewerId) {
        if (spell == null) {
            return "";
        }
        String name = FaceDownVisibility.nameShownTo(gameData, spell, viewerId);
        return unless.counters() ? " to prevent " + name + " from being countered"
                : " to keep " + name + " from being retargeted";
    }

    private int firstPayable(GameData gameData, UUID payerId, List<Ransom> alternatives, int from) {
        for (int i = Math.max(0, from); i < alternatives.size(); i++) {
            if (costPayment.couldPossiblyPay(gameData, payerId, alternatives.get(i).cost(), null)) {
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
            default -> throw new IllegalStateException("No resolution-time payment for the ransom " + cost);
        };
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
        List<Ransom> alternatives = alternatives(gameData, entry, unless);
        int asked = alternatives.size() > 1 ? entry.unlessAlternative(index) : 0;
        Ransom ransom = alternatives.get(Math.min(asked, alternatives.size() - 1));
        if (ransom.manaOrLife()) {
            // The mana and life as the question carried them: the payer may have tapped for it while asked.
            ransom = Ransom.ofManaAndLife(ability.manaCost(), ransom.xCost(), ability.additionalLifeCost());
        }
        StackEntry spell = unless.counters() || unless.retargets()
                ? spellOnStack(gameData, ability.targetCardId()) : null;
        if (accepted) {
            entry.answerUnless(index, true);
            ResolutionCostPaymentService.Outcome outcome = costPayment.pay(gameData, player, ransom.cost(),
                    new ResolutionCostPaymentService.Payment(ability.sourceCard(), ability.sourceControllerId(),
                            ability, entry));
            if (outcome != ResolutionCostPaymentService.Outcome.NOT_PAID) {
                // Paying is taking the action of "do this only once each turn" (CR 603.2h).
                TriggeredAbilityStackService.markActionTaken(gameData, entry);
            }
            if (outcome == ResolutionCostPaymentService.Outcome.ASKING) {
                if (!ransom.manaOrLife()) {
                    // Paid by the prompt's answer: the choice to pay is made now, the payment comes with the answer.
                    logPaid(gameData, player, "chooses to " + ransom.declined(), unless, spell, ability);
                }
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
        if (asked + 1 < alternatives.size()) {
            entry.askUnlessAlternative(index, asked + 1);
        } else {
            entry.answerUnless(index, false);
        }
        return true;
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
