package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AssemblyWorker;
import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.c.ChronatogTotem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoidmageHusher.class, AssemblyWorker.class, AshcoatBear.class, ChronatogTotem.class})
class VoidmageHusherTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot target a triggered ability")
    void doesNotCounterTriggeredAbility() {
        harness.addToBattlefield(player2, new VoidmageHusher());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new AshcoatBear(), "{1}{G}");
        harness.handleMayAbilityChosen(player2, true);
        harness.passPriority(player2);

        harness.castFromHand(player1, new VoidmageHusher(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(2);
        harness.assertOnBattlefield(player1, "Voidmage Husher");
        harness.assertOnBattlefield(player2, "Voidmage Husher");

        harness.passBothPriorities();
        harness.assertInHand(player2, "Voidmage Husher");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Mana abilities resolve immediately and cannot be countered")
    void doesNotCounterManaAbility() {
        Permanent totem = harness.addToBattlefieldAndReturn(player1, new ChronatogTotem());
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.stack).isEmpty();

        harness.castFromHand(player1, new VoidmageHusher(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(totem.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Voidmage Husher");
    }

    @Test
    @DisplayName("Counters an activated ability that has no targets")
    void countersNonTargetedActivatedAbility() {
        Permanent totem = harness.addToBattlefieldAndReturn(player2, new ChronatogTotem());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.activateAbility(player2, 0, 1, null, null);
        UUID abilityId = gd.stack.getLast().getTargetableId();
        harness.passPriority(player2);

        harness.castFromHand(player1, new VoidmageHusher(), "{3}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, abilityId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, totem)).isFalse();
        harness.assertOnBattlefield(player2, "Chronatog Totem");
    }

    @Test
    @DisplayName("Can counter its controller's activated ability")
    void countersOwnActivatedAbility() {
        Permanent worker = harness.addToBattlefieldAndReturn(player1, new AssemblyWorker());
        worker.setSummoningSick(false);
        harness.activateAbility(player1, 0, null, worker.getId());
        UUID abilityId = gd.stack.getLast().getTargetableId();

        harness.castFromHand(player1, new VoidmageHusher(), "{3}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, abilityId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(worker.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, worker)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Assembly-Worker");
    }

    @Test
    @DisplayName("Returns before the spell that triggered it resolves")
    void returnsBeforeTriggeringSpellResolves() {
        harness.addToBattlefield(player1, new VoidmageHusher());
        AshcoatBear bear = new AshcoatBear();
        harness.castFromHand(player1, bear, "{1}{G}");
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Voidmage Husher");
        harness.assertNotOnBattlefield(player1, "Voidmage Husher");
        harness.assertNotOnBattlefield(player1, "Ashcoat Bear");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(bear);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Counters a target activated ability when it enters")
    void countersActivatedAbilityOnEntry() {
        Permanent assemblyWorker = harness.addToBattlefieldAndReturn(player2, new AssemblyWorker());
        assemblyWorker.setSummoningSick(false);
        UUID activatedAbilityTargetId = assemblyWorker.getId();
        UUID activatedAbilityCardId = assemblyWorker.getCard().getId();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, null, activatedAbilityTargetId);
        harness.passPriority(player2);

        harness.castFromHand(player1, new VoidmageHusher(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, activatedAbilityCardId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(assemblyWorker.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, assemblyWorker)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Voidmage Husher");
    }

    @Test
    @DisplayName("May return itself to its owner's hand when its controller casts a spell")
    void mayReturnToHandWhenControllerCastsSpell() {
        harness.addToBattlefield(player1, new VoidmageHusher());
        harness.castFromHand(player1, new AshcoatBear(), "{1}{G}");
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Voidmage Husher");
        harness.assertOnBattlefield(player1, "Ashcoat Bear");
    }

    @Test
    @DisplayName("May decline returning itself when its controller casts a spell")
    void mayDeclineReturningToHand() {
        harness.addToBattlefield(player1, new VoidmageHusher());
        harness.castFromHand(player1, new AshcoatBear(), "{1}{G}");
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Voidmage Husher");
        harness.assertOnBattlefield(player1, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Does not trigger when an opponent casts a spell")
    void doesNotReturnForOpponentSpellCast() {
        harness.addToBattlefield(player1, new VoidmageHusher());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new AshcoatBear(), "{1}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Voidmage Husher");
        harness.assertOnBattlefield(player2, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Does not counter a spell with the enters-the-battlefield ability")
    void doesNotCounterSpell() {
        AshcoatBear ashcoatBear = new AshcoatBear();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, ashcoatBear, "{1}{G}");
        harness.passPriority(player2);
        harness.castFromHand(player1, new VoidmageHusher(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(ashcoatBear);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Ashcoat Bear");
    }
}
