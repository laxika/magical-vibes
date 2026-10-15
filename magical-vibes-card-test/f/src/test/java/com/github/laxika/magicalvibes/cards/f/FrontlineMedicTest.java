package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SimicManipulator;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrontlineMedic.class, Blaze.class, GrizzlyBears.class, Shock.class, SimicManipulator.class})
class FrontlineMedicTest extends BaseCardTest {

    @Test
    @DisplayName("Battalion grants indestructible to creatures you control")
    void battalionGrantsIndestructible() {
        Permanent medic = addCreatureReady(player1, new FrontlineMedic());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(medic.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(attacker.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(otherAttacker.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(opposingCreature.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Battalion does not trigger without two other attackers")
    void battalionDoesNotTriggerWithFewerThanTwoOtherAttackers() {
        Permanent medic = addCreatureReady(player1, new FrontlineMedic());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(medic.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Battalion's indestructible grant wears off at end of turn")
    void battalionIndestructibleWearsOff() {
        Permanent medic = addCreatureReady(player1, new FrontlineMedic());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();
        assertThat(medic.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(medic.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Sacrifice ability counters an X spell when its controller cannot pay")
    void countersXSpellWhenControllerCannotPay() {
        harness.addToBattlefield(player1, new FrontlineMedic());

        Blaze blaze = new Blaze();
        harness.setHand(player2, List.of(blaze));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 2, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, blaze.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Blaze");
        harness.assertInGraveyard(player1, "Frontline Medic");
    }

    @Test
    @DisplayName("Sacrifice ability lets the controller pay {3} to keep an X spell")
    void XSpellSurvivesWhenControllerPays() {
        harness.addToBattlefield(player1, new FrontlineMedic());

        Blaze blaze = new Blaze();
        harness.setHand(player2, List.of(blaze));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 2, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, blaze.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Frontline Medic");
        harness.assertInGraveyard(player2, "Blaze");
    }

    @Test
    @DisplayName("Sacrifice ability cannot target a spell without X in its mana cost")
    void cannotTargetNonXSpell() {
        harness.addToBattlefield(player1, new FrontlineMedic());

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void battalionDoesNotTriggerWhenMedicDoesNotAttack() {
        Permanent medic = addCreatureReady(player1, new FrontlineMedic());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(medic.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(attacker.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void battalionIncludesNonattackersAndOnlyCreaturesPresentAtResolution() {
        addCreatureReady(player1, new FrontlineMedic());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(nonattacker.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(beforeResolution.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(afterResolution.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void battalionDoesNotGrantIndestructibleToMedicTakenByOpponentBeforeResolution() {
        Permanent medic = addCreatureReady(player1, new FrontlineMedic());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent manipulator = addCreatureReady(player2, new SimicManipulator());
        manipulator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        declareAttackers(player1, List.of(0, 1, 2));
        harness.activateAbility(player2, 0, 3, medic.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(medic);
        assertThat(attacker.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(medic.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(manipulator.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void battalionStillResolvesAfterAnotherAttackerDies() {
        Permanent medic = addCreatureReady(player1, new FrontlineMedic());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        declareAttackers(player1, List.of(0, 1, 2));
        harness.castInstant(player2, 0, otherAttacker.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(otherAttacker);
        assertThat(medic.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(attacker.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void countersXSpellWhenControllerDeclinesAffordablePayment() {
        harness.addToBattlefield(player1, new FrontlineMedic());
        Blaze blaze = new Blaze();
        harness.setHand(player2, List.of(blaze));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 2, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, blaze.getId());
        harness.assertInGraveyard(player1, "Frontline Medic");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Blaze");
    }

    @Test
    void canCounterSpellWithXChosenAsZero() {
        harness.addToBattlefield(player1, new FrontlineMedic());
        Blaze blaze = new Blaze();
        harness.setHand(player2, List.of(blaze));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, blaze.getId());
        harness.assertInGraveyard(player1, "Frontline Medic");
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Blaze");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
