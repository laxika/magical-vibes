package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CommissarSeverinaRaine.class, GrizzlyBears.class, Forest.class})
class CommissarSeverinaRaineTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with other creatures makes each opponent lose life for each of them")
    void attacksDrainForOtherAttackers() {
        Permanent commissar = addCreatureReady(player1, new CommissarSeverinaRaine());
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent thirdBlocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(commissar),
                gd.playerBattlefields.get(player1.getId()).indexOf(firstBear),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondBear)));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(firstBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(commissar)),
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(secondBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(firstBear)),
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(thirdBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(secondBear))));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Sacrificing another creature gains life and draws a card")
    void sacrificesAnotherCreatureGainsLifeAndDraws() {
        harness.addToBattlefield(player1, new CommissarSeverinaRaine());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Commissar Severina Raine");
    }

    @Test
    @DisplayName("Attacking alone does not cause life loss even with nonattacking creatures present")
    void attackingAloneDoesNotDrain() {
        addCreatureReady(player1, new CommissarSeverinaRaine());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Other creatures attacking without the Commissar do not trigger life loss")
    void doesNotTriggerWhenNotAttacking() {
        addCreatureReady(player1, new CommissarSeverinaRaine());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            resolveAllTriggers();
        });

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The Commissar cannot sacrifice itself or an opponent's creature")
    void cannotActivateWithoutAnotherControlledCreature() {
        harness.addToBattlefield(player1, new CommissarSeverinaRaine());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Commissar Severina Raine");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Commissar can activate, paying the sacrifice before resolution")
    void sacrificeIsPaidBeforeLifeGainAndDraw() {
        harness.addToBattlefield(player1, new CommissarSeverinaRaine());
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 10);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertInHand(player1, "Forest");
        harness.assertOnBattlefield(player1, "Commissar Severina Raine");
    }

    @Test
    @DisplayName("Sacrificing the other attacker in response reduces X when the attack trigger resolves")
    void countsAttackersAtResolution() {
        addCreatureReady(player1, new CommissarSeverinaRaine());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            harness.activateAbility(player1, 0, null, null);
            resolveAllTriggers();
        });

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 12);
        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Forest");
    }
}
