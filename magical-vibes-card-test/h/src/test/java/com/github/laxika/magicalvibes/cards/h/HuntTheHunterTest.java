package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HuntTheHunter.class, GrizzlyBears.class, LlanowarElves.class, HillGiant.class, NessianCourser.class})
class HuntTheHunterTest extends BaseCardTest {

    @Test
    @DisplayName("The green creature gets +2/+2 before fighting the opponent's green creature")
    void boostsBeforeFight() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new HuntTheHunter()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveSorcery(player1, 0, List.of(bearId, elvesId));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Llanowar Elves");

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
    }

    @Test
    @DisplayName("The +2/+2 wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new HuntTheHunter()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveSorcery(player1, 0, List.of(bearId, elvesId));

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-green creature you control")
    void cannotTargetNonGreenCreatureYouControl() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new HuntTheHunter()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(giantId, elvesId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-green creature an opponent controls")
    void cannotTargetNonGreenOpponentCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new HuntTheHunter()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearId, giantId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stillBoostsWhenOpponentTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new HuntTheHunter()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent elves = gd.playerBattlefields.get(player2.getId()).getFirst();

        harness.castSorcery(player1, 0, List.of(bear.getId(), elves.getId()));
        harness.getPermanentRemovalService().removePermanentToHand(gd, elves);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        assertThat(bear.getMarkedDamage()).isZero();
        harness.assertInHand(player2, "Llanowar Elves");
    }

    @Test
    void noFightWhenYourTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new HuntTheHunter()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent elves = gd.playerBattlefields.get(player2.getId()).getFirst();

        harness.castSorcery(player1, 0, List.of(bear.getId(), elves.getId()));
        harness.getPermanentRemovalService().removePermanentToHand(gd, bear);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(elves.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(1);
    }

    @Test
    void cannotReverseTheControllersOfTheTargets() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new HuntTheHunter()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(elvesId, bearId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotFightAnotherCreatureYouControl() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new HuntTheHunter()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearId, elvesId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boostLetsYourCreatureSurviveAnOtherwiseLethalFight() {
        harness.addToBattlefield(player1, new NessianCourser());
        harness.addToBattlefield(player2, new NessianCourser());
        harness.setHand(player1, List.of(new HuntTheHunter()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        Permanent courser = gd.playerBattlefields.get(player1.getId()).getFirst();
        UUID opponentId = harness.getPermanentId(player2, "Nessian Courser");

        harness.castAndResolveSorcery(player1, 0, List.of(courser.getId(), opponentId));

        harness.assertOnBattlefield(player1, "Nessian Courser");
        harness.assertInGraveyard(player2, "Nessian Courser");
        assertThat(gqs.getEffectivePower(gd, courser)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, courser)).isEqualTo(5);
        assertThat(courser.getMarkedDamage()).isEqualTo(3);
    }
}
