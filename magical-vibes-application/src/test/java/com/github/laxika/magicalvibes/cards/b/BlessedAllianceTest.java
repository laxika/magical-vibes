package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlessedAllianceTest extends BaseCardTest {

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Life mode gives 4 life to the target player")
    void lifeModeGivesFourLife() {
        harness.setHand(player1, List.of(new BlessedAlliance()));
        addBaseMana();
        gd.playerLifeTotals.put(player2.getId(), 10);

        harness.castModalInstantWithModes(player1, 0, 1, 3, new int[]{0}, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Untap mode untaps up to two target creatures")
    void untapModeUntapsTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.tap();
        second.tap();
        harness.setHand(player1, List.of(new BlessedAlliance()));
        addBaseMana();

        harness.castModalInstantWithModes(player1, 0, 1, 3, new int[]{1},
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Sacrifice mode sacrifices an attacking creature of the target opponent's choice")
    void sacrificeModeSacrificesAttackingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of(new BlessedAlliance()));
        addBaseMana();

        harness.castModalInstantWithModes(player1, 0, 1, 3, new int[]{2}, List.of(player2.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Two modes can target the same player and require escalate mana")
    void twoModesSharePlayerTarget() {
        harness.setHand(player1, List.of(new BlessedAlliance()));
        addBaseMana();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gd.playerLifeTotals.put(player2.getId(), 10);

        harness.castModalInstantWithModes(player1, 0, 1, 3, new int[]{0, 2},
                List.of(player2.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Life mode rejects a non-player target")
    void lifeModeRejectsNonPlayerTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlessedAlliance()));
        addBaseMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 3, new int[]{0}, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
