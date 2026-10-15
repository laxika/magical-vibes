package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrizzlyBears.class, Humble.class, PhyrexianColossus.class})
class PhyrexianColossusTest extends BaseCardTest {
    @Test
    void paysLifeImmediatelyButUntapsOnlyOnResolution() {
        Permanent colossus = addCreatureReady(player1, new PhyrexianColossus());
        colossus.tap();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(colossus.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(colossus.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    void canActivateWhileSummoningSickAndUntapsOnlyItself() {
        Permanent colossus = harness.addToBattlefieldAndReturn(player1, new PhyrexianColossus());
        colossus.setSummoningSick(true);
        colossus.tap();
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        other.tap();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(colossus.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    void canBeBlockedByMoreThanThreeCreatures() {
        addCreatureReady(player1, new PhyrexianColossus());
        for (int i = 0; i < 4; i++) {
            addCreatureReady(player2, new GrizzlyBears());
        }
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0), new BlockerAssignment(3, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId())).allMatch(Permanent::isBlocking);
    }

    @Test
    @CardUsed({Humble.class, GrizzlyBears.class, PhyrexianColossus.class})
    void losingAbilitiesAllowsASingleBlocker() {
        Permanent colossus = addCreatureReady(player1, new PhyrexianColossus());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, colossus.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasLostPrintedAbilities(gd, colossus)).isTrue();
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(bear.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tapped Phyrexian Colossus does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent colossus = addCreatureReady(player1, new PhyrexianColossus());
        colossus.tap();

        advanceToUpkeep(player1);

        assertThat(colossus.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying 8 life untaps Phyrexian Colossus")
    void payLifeUntaps() {
        Permanent colossus = addCreatureReady(player1, new PhyrexianColossus());
        colossus.tap();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(colossus.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate untap ability with fewer than 8 life")
    void cannotActivateWithInsufficientLife() {
        addCreatureReady(player1, new PhyrexianColossus());
        harness.setLife(player1, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("Cannot be blocked by fewer than three creatures")
    void cannotBeBlockedByFewerThanThree() {
        addCreatureReady(player1, new PhyrexianColossus());

        for (int i = 0; i < 3; i++) {
            addCreatureReady(player2, new GrizzlyBears());
        }

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("3 or more creatures");
    }

    @Test
    @DisplayName("Cannot be blocked by fewer than three creatures")
    void cannotBeBlockedByFewerThanThreeUpstreamReview() {
        addCreatureReady(player1, new PhyrexianColossus());

        for (int i = 0; i < 3; i++) {
            addCreatureReady(player2, new GrizzlyBears());
        }

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("3 or more creatures");
    }

    @Test
    @DisplayName("Can be blocked by three creatures")
    void canBeBlockedByThree() {
        addCreatureReady(player1, new PhyrexianColossus());

        for (int i = 0; i < 3; i++) {
            addCreatureReady(player2, new GrizzlyBears());
        }

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can be blocked by three creatures")
    void canBeBlockedByThreeUpstreamReview() {
        addCreatureReady(player1, new PhyrexianColossus());

        for (int i = 0; i < 3; i++) {
            addCreatureReady(player2, new GrizzlyBears());
        }

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .allMatch(Permanent::isBlocking);
    }

    @Test
    @DisplayName("Paying 8 life does not require Phyrexian Colossus to be tapped")
    void payLifeAbilityDoesNotRequireTap() {
        Permanent colossus = addCreatureReady(player1, new PhyrexianColossus());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(colossus.isTapped()).isFalse();
    }
}
