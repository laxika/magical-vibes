package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SigiledSentinel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A limit, and a "one or more" mark, is its ability's own: two limited abilities of one permanent each keep theirs
 * ("triggers only once each turn", "do this only once each turn" as the action is taken, CR 603.2h), and a mark is
 * used when the ability triggers, not while an event that doesn't trigger it is judged. "For the first time each turn"
 * is part of the event: the first attack of the turn, whatever the ability's conditions then say.
 */
@CardUsed({GrizzlyBears.class, Shock.class, SigiledSentinel.class})
class AbilityLimitMarkTest extends BaseCardTest {

    private static final TriggerOptions ONCE_EACH_TURN = TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN);

    private static Card creature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private static Card permanent(String name, CardType type, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setSubtypes(List.of(subtypes));
        card.setManaCost("{1}");
        return card;
    }

    private List<StackEntry> entriesOf(String name) {
        return gd.stack.stream().filter(entry -> entry.getCard().getName().equals(name)).toList();
    }

    private List<Integer> effectCountsOf(String name) {
        return entriesOf(name).stream().map(entry -> entry.getEffectsToResolve().size()).toList();
    }

    private Permanent attacking(Card card) {
        Permanent attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
        return attacker;
    }

    private void attackWith(int index) {
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(player1, List.of(index)));
    }

    private void attackAgainWith(Permanent attacker, int index) {
        attacker.untap();
        attacker.setAttacking(false);
        attackWith(index);
    }

    @Test
    @DisplayName("Two \"whenever you attack\" abilities of one permanent that trigger only once each turn each trigger")
    void twoOnceEachTurnYouAttackAbilitiesEachTrigger() {
        Card card = creature("Twin Drummer");
        card.addTrigger(Triggers.youAttack(), ONCE_EACH_TURN, new DrawCardEffect(1));
        card.addTrigger(Triggers.youAttack(), ONCE_EACH_TURN, new GainLifeEffect(1));
        Permanent drummer = addCreatureReady(player1, card);

        attackWith(0);

        assertThat(entriesOf("Twin Drummer")).hasSize(2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        attackAgainWith(drummer, 0);
        assertThat(entriesOf("Twin Drummer")).isEmpty();
    }

    @Test
    @DisplayName("An attack ability that triggers only once each turn is unused by an attack it didn't trigger for")
    void onceEachTurnAttackAbilityIsUsedOnlyWhenItTriggers() {
        Card card = creature("Late Bloomer");
        card.addTrigger(Triggers.attacks(),
                ONCE_EACH_TURN.withInterveningIf(new ControllerLifeAtLeast(30, false)), new GainLifeEffect(1));
        Permanent bloomer = addCreatureReady(player1, card);
        harness.setLife(player1, 20);

        attackWith(0);
        assertThat(entriesOf("Late Bloomer")).isEmpty();

        harness.setLife(player1, 30);
        attackAgainWith(bloomer, 0);
        assertThat(entriesOf("Late Bloomer")).hasSize(1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);
        attackAgainWith(bloomer, 0);
        assertThat(entriesOf("Late Bloomer")).isEmpty();
    }

    @Test
    @DisplayName("\"Attacks for the first time each turn\" is the first attack, whether or not its \"if\" let it trigger")
    void firstTimeEachTurnAttackAbilityIsTheFirstAttack() {
        TriggerOptions firstTime = TriggerOptions.DEFAULT.withLimit(TriggerLimit.FIRST_TIME_EACH_TURN)
                .withInterveningIf(new ControllerLifeAtLeast(30, false));
        Card late = creature("Missed Chance");
        late.addTrigger(Triggers.attacks(), firstTime, new GainLifeEffect(1));
        Permanent missed = addCreatureReady(player1, late);
        harness.setLife(player1, 20);

        attackWith(0);
        assertThat(entriesOf("Missed Chance")).isEmpty();

        harness.setLife(player1, 30);
        attackAgainWith(missed, 0);
        assertThat(entriesOf("Missed Chance")).isEmpty();

        Card early = creature("Taken Chance");
        early.addTrigger(Triggers.attacks(), firstTime, new GainLifeEffect(1));
        addCreatureReady(player1, early);
        attackWith(1);
        assertThat(entriesOf("Taken Chance")).hasSize(1);
    }

    @Test
    @DisplayName("An Equipment's \"blocks one or more Bears\" triggers when only the second creature blocked is a Bear")
    void attachedBlockAbilityTriggersForTheSecondCreatureBlocked() {
        Card wall = creature("Wide Wall");
        wall.addStaticEffect(new CanBlockAnyNumberOfCreaturesEffect());
        Card trap = permanent("Bear Trap", CardType.ARTIFACT, CardSubtype.EQUIPMENT);
        trap.addTrigger(Triggers.blocks().withFilter(new PermanentHasSubtypePredicate(CardSubtype.BEAR)),
                TriggerOptions.ONE_OR_MORE, new GainLifeEffect(1));
        attacking(creature("Plain Raider"));
        attacking(new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, wall);
        addCreatureReady(player2, trap).setAttachedTo(blocker.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));

        assertThat(entriesOf("Bear Trap")).hasSize(1);
    }

    @Test
    @DisplayName("Taking one \"do this only once each turn\" action leaves another such ability of the permanent free")
    void twoDoOnlyOnceEachTurnAbilitiesKeepTheirOwnMark() {
        TriggerOptions doOnce = TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN);
        Card card = permanent("Twin Patron", CardType.ENCHANTMENT);
        card.addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(), doOnce,
                new MayEffect(new DrawCardEffect(1), "Draw a card?"));
        card.addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(), doOnce,
                new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
        harness.addToBattlefield(player1, card);
        attacking(new GrizzlyBears());
        harness.setHand(player1, List.of());
        int lifeBefore = gd.getLife(player1.getId());

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);
        assertThat(entriesOf("Twin Patron")).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Two \"one or more permanents you control become tapped\" abilities with the same first effect each trigger once")
    void twoOneOrMoreTapAbilitiesEachTriggerOnceForABatch() {
        Card card = permanent("Twin Tollkeeper", CardType.ENCHANTMENT);
        card.addTrigger(Triggers.permanentYouControlBecomesTapped(), TriggerOptions.ONE_OR_MORE,
                new GainLifeEffect(1));
        card.addTrigger(Triggers.permanentYouControlBecomesTapped(), TriggerOptions.ONE_OR_MORE,
                new GainLifeEffect(1), new DrawCardEffect(1));
        harness.addToBattlefield(player1, card);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        TriggerCollectionService collection = GameTestEngineContext.get().getBean(TriggerCollectionService.class);

        harness.inMutationScope(() -> {
            collection.beginPermanentTapTriggerBatch(gd);
            first.tap();
            collection.checkEnchantedPermanentTapTriggers(gd, first, player1.getId());
            second.tap();
            collection.checkEnchantedPermanentTapTriggers(gd, second, player1.getId());
            collection.endPermanentTapTriggerBatch(gd);
        });

        assertThat(effectCountsOf("Twin Tollkeeper")).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    @DisplayName("Two \"an opponent loses life for the first time during each of their turns\" abilities of one permanent each trigger")
    void twoFirstOpponentLifeLossAbilitiesEachTrigger() {
        Card card = permanent("Twin Harrower", CardType.ENCHANTMENT);
        card.addTrigger(Triggers.opponentLosesLifeForFirstTimeDuringTheirTurn(), new GainLifeEffect(1));
        card.addTrigger(Triggers.opponentLosesLifeForFirstTimeDuringTheirTurn(), new DrawCardEffect(1));
        harness.addToBattlefield(player1, card);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveInstant(player2, 0, player2.getId());

        assertThat(entriesOf("Twin Harrower")).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0, player2.getId());
        assertThat(entriesOf("Twin Harrower")).isEmpty();
    }

    @Test
    @DisplayName("A \"becomes the target of a backup ability\" ability without a limit triggers for each backup ability")
    void unlimitedBackupTargetAbilityTriggersEachTime() {
        Card card = permanent("Backup Chronicler", CardType.ENCHANTMENT);
        card.addTrigger(Triggers.creatureYouControlBecomesTargetOfBackupAbility(), new GainLifeEffect(1));
        harness.addToBattlefield(player1, card);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SigiledSentinel(), new SigiledSentinel()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        assertThat(entriesOf("Backup Chronicler")).hasSize(1);
        resolveAllTriggers();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        assertThat(entriesOf("Backup Chronicler")).hasSize(1);
    }
}
