package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianColossus.class, ArgothianSwine.class})
class PhyrexianColossusTest extends BaseCardTest {

    // ===== Doesn't untap during untap step =====

    @Test
    @DisplayName("Tapped Phyrexian Colossus does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent colossus = addCreatureReady(player1, new PhyrexianColossus());
        colossus.tap();

        advanceToUpkeep(player1);

        assertThat(colossus.isTapped()).isTrue();
    }

    // ===== Activated ability: pay 8 life to untap =====

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
    @DisplayName("Paying 8 life does not require Phyrexian Colossus to be tapped")
    void payLifeAbilityDoesNotRequireTap() {
        Permanent colossus = addCreatureReady(player1, new PhyrexianColossus());
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

    // ===== Blocking restriction: can't be blocked by fewer than three =====

    @Test
    @DisplayName("Cannot be blocked by fewer than three creatures")
    void cannotBeBlockedByFewerThanThree() {
        addCreatureReady(player1, new PhyrexianColossus());

        for (int i = 0; i < 3; i++) {
            addCreatureReady(player2, new ArgothianSwine());
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
            addCreatureReady(player2, new ArgothianSwine());
        }

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .allMatch(Permanent::isBlocking);
    }
}
