package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CostEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.effect.cost.ResolutionCostPaymentService;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * "[Do something] unless [a player pays a cost]" (CR 118.12a). The payer is asked as the effect resolves, the
 * resolution waiting on this effect; the answer ({@code MayAbilityHandlerService}) pays the cost and the resolution
 * runs this effect again, which {@code EffectResolutionService} then resolves as the branch the answer chose
 * ({@code ifPaid} or {@code ifNotPaid}, CR 118.12: the choice to pay, not what happened). A payer who couldn't pay the
 * cost in full isn't asked and {@code ifNotPaid} resolves at once ({@link #cannotBePaid}, CR 118.3, 608.2d).
 *
 * <p>What can pay: the payer's mana pool and the mana they could still make (they may activate mana abilities while
 * asked, CR 605.3a); mana that can be spent only on {X} for an announced-X ransom; 2 life for each {B} under K'rrik,
 * Son of Yawgmoth; life only while the payer can lose life (CR 119.4, 119.8); all of it judged and paid by
 * {@link ResolutionCostPaymentService}, which also raises the tax-paid event for mana paid for another player's spell
 * or permanent. An amount read from the source reads the source on the battlefield, else as it last existed
 * (CR 608.2h).
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
     * The ransom as it is paid now: the mana cost ({@code null} for none), whether it is the announced X, and the life.
     *
     * @param manaCost the mana to pay, or {@code null} when the ransom is life only
     * @param xCost    whether mana spendable only on {X} may pay it
     * @param life     the life to pay with the mana
     */
    public record Ransom(String manaCost, boolean xCost, int life) {

        /** The cost as the prompt and the log read it: "{2}", "3 life", "{2} and 2 life". */
        public String text() {
            if (manaCost == null) {
                return life + " life";
            }
            return life > 0 ? manaCost + " and " + life + " life" : manaCost;
        }

        /** The ransom as the cost {@link ResolutionCostPaymentService} pays. */
        public CostEffect cost() {
            if (manaCost == null) {
                return new PayLifeCost(life);
            }
            return new PayManaCost(manaCost, null, null, false, life, false, xCost);
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
        if (payerId == null) {
            return;
        }
        Ransom ransom = ransom(gameData, entry, unless);
        StackEntry spell = unless.counters()
                ? subjectResolutionService.spellOnStack(gameData, entry, Subjects.targetSpell()) : null;
        String prompt = spell == null
                ? "Pay " + ransom.text() + "?"
                : "Pay " + ransom.text() + " to prevent " + FaceDownVisibility.nameShownTo(gameData, spell, payerId)
                        + " from being countered?";
        gameData.resolvingMayEffectFromStack = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), payerId, List.of(unless), prompt,
                spell == null ? entry.getTargetId() : spell.getTargetableId(), ransom.manaCost(),
                entry.getSourcePermanentId(), null, 0, ransom.life(), null, null, null,
                entry.getSourcePermanentSnapshot(), entry.getControllerId()));
    }

    /**
     * Whether {@code unless}'s payer could not pay its ransom in full now, so is not asked and its {@code ifNotPaid}
     * happens (CR 118.3, 608.2d). A payer the subject no longer names (the spell left the stack) is asked nothing and
     * nothing happens, which {@link #resolve} decides.
     */
    public boolean cannotBePaid(GameData gameData, StackEntry entry, UnlessPaysEffect unless) {
        UUID payerId = payer(gameData, entry, unless);
        return payerId != null && !canPay(gameData, payerId, ransom(gameData, entry, unless));
    }

    /**
     * The player who may pay, or {@code null}: none for a counter whose spell or ability is no longer on the stack (an
     * earlier instruction countered it "instead", Bring the Ending), as there is nothing left to pay for.
     */
    private UUID payer(GameData gameData, StackEntry entry, UnlessPaysEffect unless) {
        if (unless.counters() && subjectResolutionService.spellOnStack(gameData, entry, Subjects.targetSpell()) == null) {
            return null;
        }
        return subjectResolutionService.player(gameData, entry, unless, unless.payer());
    }

    /** {@code unless}'s ransom with its amounts read as it resolves. */
    public Ransom ransom(GameData gameData, StackEntry entry, UnlessPaysEffect unless) {
        return switch (unless.ransom()) {
            case PayManaCost mana -> new Ransom(manaCost(gameData, entry, mana), mana.announcedX(), mana.lifeAmount());
            case PayLifeCost life -> new Ransom(null, false,
                    amountEvaluationService.evaluate(gameData, life.amount(), amountContext(gameData, entry)));
            default -> throw new IllegalStateException("Not a mana or life ransom: " + unless.ransom());
        };
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

    /**
     * Whether {@code payerId} could pay {@code ransom} in full (CR 118.3): the mana from their pool or the mana they
     * could make, {B} paid with 2 life under K'rrik included, and the life while they can lose life (CR 119.8).
     */
    private boolean canPay(GameData gameData, UUID payerId, Ransom ransom) {
        return costPayment.canPay(gameData, payerId, ransom.cost(), null);
    }

    /** Whether {@code playerId} could pay {@code amount} life (CR 119.4, 119.8): {@link ResolutionCostPaymentService#canPayLife}. */
    public boolean canPayLife(GameData gameData, UUID playerId, int amount) {
        return costPayment.canPayLife(gameData, playerId, amount);
    }

    /**
     * {@code player}'s answer to {@code ability}, the question {@link #resolve} asked: accepted and payable, the ransom
     * is paid (the mana from their pool, {B} with 2 life under K'rrik when the mana falls short, then the life) and
     * {@code true} returned; declined, or accepted without the resources, {@code false} (CR 118.3). Paying life is
     * losing it (CR 119.4): a Lich's Duel Mastery sacrifice it would cause has its death replacements chosen first
     * (CR 616.1), and {@code null} is returned while they are asked; the answer is then completed again.
     */
    public Boolean answer(GameData gameData, Player player, boolean accepted, PendingMayAbility ability,
                          UnlessPaysEffect unless) {
        UUID payerId = player.getId();
        Ransom ransom = new Ransom(ability.manaCost(), unless.ransom() instanceof PayManaCost mana && mana.announcedX(),
                ability.additionalLifeCost());
        StackEntry spell = unless.counters() ? spellOnStack(gameData, ability.targetCardId()) : null;
        if (accepted) {
            ResolutionCostPaymentService.Outcome outcome = costPayment.pay(gameData, player, ransom.cost(),
                    new ResolutionCostPaymentService.Payment(ability.sourceCard(), ability.sourceControllerId(), ability));
            if (outcome == ResolutionCostPaymentService.Outcome.ASKING) {
                return null;
            }
            if (outcome == ResolutionCostPaymentService.Outcome.PAID) {
                if (spell != null) {
                    gameLogService.append(gameData, GameLog.textCardText(
                            player.getUsername() + " pays " + ransom.text() + ". ", spell.getCard(), " is not countered."));
                } else {
                    gameLogService.append(gameData, GameLog.text(player.getUsername() + " pays " + ransom.text() + "."));
                }
                log.info("Game {} - {} pays {} ({})", gameData.id, player.getUsername(), ransom.text(),
                        ability.sourceCard().getName());
                return true;
            }
        }
        if (spell != null && !counterable(gameData, spell, ability)) {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " declines to pay " + ransom.text() + ". ", spell.getCard(),
                    " cannot be countered."));
        } else {
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " declines to pay " + ransom.text() + "."));
        }
        log.info("Game {} - {} declines to pay {} ({})", gameData.id, player.getUsername(), ransom.text(),
                ability.sourceCard().getName());
        return false;
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
