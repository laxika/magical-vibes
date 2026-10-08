package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
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

@CardUsed({VindictiveWarden.class})
class VindictiveWardenTest extends BaseCardTest {

    @Test
    @DisplayName("Firebending adds one red mana until end of combat")
    void firebendingAddsManaUntilEndOfCombat() {
        Permanent warden = addReadyWarden();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(warden.isTapped()).isTrue();

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("The activated ability deals one damage to each opponent")
    void activatedAbilityDealsDamageToEachOpponent() {
        addReadyWarden();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife - 1);
    }

    @Test
    @DisplayName("Menace rejects a single blocker")
    void menaceRejectsSingleBlocker() {
        addReadyWarden();
        addCreatureReady(player2, new VindictiveWarden());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Menace permits two blockers")
    void menacePermitsTwoBlockers() {
        addReadyWarden();
        Permanent first = addCreatureReady(player2, new VindictiveWarden());
        Permanent second = addCreatureReady(player2, new VindictiveWarden());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A tapped summoning-sick Warden can activate repeatedly without damaging its controller")
    void activatedAbilityWorksRepeatedlyWhileTappedAndSummoningSick() {
        Permanent warden = addReadyWarden();
        warden.setSummoningSick(true);
        warden.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        int controllerLife = gd.playerLifeTotals.get(player1.getId());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLife);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife - 2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(warden.isTapped()).isTrue();
    }

    private Permanent addReadyWarden() {
        return addCreatureReady(player1, new VindictiveWarden());
    }
}
