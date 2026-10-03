package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AdarkarWastes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BorrowedMalevolence.class, GrizzlyBears.class, AdarkarWastes.class, Unsummon.class})
class BorrowedMalevolenceTest extends BaseCardTest {

    @Test
    @DisplayName("Boost mode gives target creature +1/+1 until end of turn")
    void boostModeGivesPlusOnePlusOne() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorrowedMalevolence()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Malus mode gives target creature -1/-1 until end of turn")
    void malusModeGivesMinusOneMinusOne() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorrowedMalevolence()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{1}, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(-1);
        assertThat(creature.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Both modes can target the same creature and pay escalate mana")
    void bothModesShareTargetAndEscalate() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorrowedMalevolence()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(creature.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Both modes without escalate mana are rejected")
    void bothModesWithoutEscalateManaRejected() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorrowedMalevolence()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A noncreature cannot be targeted")
    void rejectsNoncreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new AdarkarWastes());
        harness.setHand(player1, List.of(new BorrowedMalevolence()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{0}, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Both modes affect only their respective targets and expire at cleanup")
    void distinctTargetsReceiveTheirOwnModifiersUntilEndOfTurn() {
        Permanent boosted = addCreatureReady(player1, new GrizzlyBears());
        Permanent weakened = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorrowedMalevolence()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(boosted.getId(), weakened.getId()));
        harness.passBothPriorities();

        assertThat(boosted.getPowerModifier()).isEqualTo(1);
        assertThat(boosted.getToughnessModifier()).isEqualTo(1);
        assertThat(weakened.getPowerModifier()).isEqualTo(-1);
        assertThat(weakened.getToughnessModifier()).isEqualTo(-1);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(boosted.getPowerModifier()).isZero();
        assertThat(boosted.getToughnessModifier()).isZero();
        assertThat(weakened.getPowerModifier()).isZero();
        assertThat(weakened.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The second mode still resolves when the first target leaves the battlefield")
    void remainingTargetStillGetsMinusOneMinusOne() {
        Permanent bounced = addCreatureReady(player1, new GrizzlyBears());
        Permanent weakened = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorrowedMalevolence()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(bounced.getId(), weakened.getId()));
        harness.castInstant(player2, 0, bounced.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(weakened.getPowerModifier()).isEqualTo(-1);
        assertThat(weakened.getToughnessModifier()).isEqualTo(-1);
        harness.assertInGraveyard(player1, "Borrowed Malevolence");
    }

    @Test
    @DisplayName("The first mode still resolves when the second target leaves the battlefield")
    void remainingTargetStillGetsPlusOnePlusOne() {
        Permanent boosted = addCreatureReady(player1, new GrizzlyBears());
        Permanent bounced = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorrowedMalevolence()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(boosted.getId(), bounced.getId()));
        harness.castInstant(player2, 0, bounced.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(boosted.getPowerModifier()).isEqualTo(1);
        assertThat(boosted.getToughnessModifier()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Borrowed Malevolence");
    }

    @Test
    @DisplayName("A creature with one remaining toughness survives both modes on itself")
    void sharedTargetWithOneRemainingToughnessSurvivesBothModes() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorrowedMalevolence(), new BorrowedMalevolence()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{1}, List.of(creature.getId()));
        harness.passBothPriorities();
        assertThat(creature.getToughnessModifier()).isEqualTo(-1);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(creature.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(creature.getPowerModifier()).isEqualTo(-1);
        assertThat(creature.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Repeated minus modes put a creature with zero toughness into the graveyard")
    void minusModeCanKillACreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorrowedMalevolence(), new BorrowedMalevolence()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{1}, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{1}, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Neither mode resolves when their shared target leaves the battlefield")
    void bothModesLoseTheirSharedTarget() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorrowedMalevolence()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(creature.getId(), creature.getId()));
        harness.castInstant(player2, 0, creature.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Borrowed Malevolence");
        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
