package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AxegrinderGiant;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.j.JaceMemoryAdept;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuardianOfTheAges.class, AxegrinderGiant.class, JaceMemoryAdept.class, Disperse.class, GuardDuty.class})
class GuardianOfTheAgesTest extends BaseCardTest {

    @Test
    @DisplayName("When attacked, loses defender and gains trample")
    void losesDefenderAndGainsTrampleWhenAttacked() {
        Permanent guardian = addCreatureReady(player1, new GuardianOfTheAges());
        addCreatureReady(player2, new AxegrinderGiant());

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isFalse();

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Defender loss and trample gain last past end of turn")
    void keywordChangePersistsPastEndOfTurn() {
        Permanent guardian = addCreatureReady(player1, new GuardianOfTheAges());
        addCreatureReady(player2, new AxegrinderGiant());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger again after losing defender")
    void doesNotRetriggerWithoutDefender() {
        Permanent guardian = addCreatureReady(player1, new GuardianOfTheAges());
        Permanent attacker = addCreatureReady(player2, new AxegrinderGiant());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.DEFENDER)).isFalse();
        assertThat(gd.stack).isEmpty();

        // New combat after the guardian already lost defender — intervening if skips the trigger.
        attacker.untap();
        attacker.setAttacking(false);
        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Attacking a planeswalker controlled by the Guardian's controller triggers it")
    void triggersWhenPlaneswalkerIsAttacked() {
        Permanent guardian = addCreatureReady(player1, new GuardianOfTheAges());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceMemoryAdept());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        addCreatureReady(player2, new AxegrinderGiant());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, planeswalker.getId()));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Each attacker triggers the ability before any trigger resolves")
    void multipleAttackersTriggerSeparately() {
        Permanent guardian = addCreatureReady(player1, new GuardianOfTheAges());
        addCreatureReady(player2, new AxegrinderGiant());
        addCreatureReady(player2, new AxegrinderGiant());

        declareAttackers(player2, List.of(0, 1));

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, guardian, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declaring no attackers leaves defender unchanged")
    void noAttackersDoesNotTrigger() {
        Permanent guardian = addCreatureReady(player1, new GuardianOfTheAges());
        addCreatureReady(player2, new AxegrinderGiant());

        declareAttackers(player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Returning the attacker to hand does not counter the Guardian's trigger")
    void triggerResolvesAfterAttackerLeaves() {
        Permanent guardian = addCreatureReady(player1, new GuardianOfTheAges());
        Permanent attacker = addCreatureReady(player2, new AxegrinderGiant());
        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player2, List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, attacker.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Axegrinder Giant");
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The ability triggers again after a later Aura grants defender")
    void triggersAgainAfterRegainingDefender() {
        Permanent guardian = addCreatureReady(player1, new GuardianOfTheAges());
        Permanent attacker = addCreatureReady(player2, new AxegrinderGiant());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.DEFENDER)).isFalse();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GuardDuty()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.ensurePriority(player1);
        harness.castEnchantment(player1, 0, guardian.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.DEFENDER)).isTrue();

        attacker.untap();
        attacker.setAttacking(false);
        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, guardian, Keyword.TRAMPLE)).isTrue();
    }
}
