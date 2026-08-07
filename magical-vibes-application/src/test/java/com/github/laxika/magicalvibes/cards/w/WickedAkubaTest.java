package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WickedAkubaTest extends BaseCardTest {

    private static final int STARTING_LIFE = 20;

    private void addReadyAkuba() {
        addCreatureReady(player1, new WickedAkuba());
    }

    @Test
    @DisplayName("A player dealt combat damage by Wicked Akuba loses 1 life to the ability")
    void damagedPlayerLosesLife() {
        addReadyAkuba();

        declareAttackers(List.of(0));
        resolveCombat();
        // Mana empties between steps, so pay for the ability after combat damage is dealt.
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(STARTING_LIFE - 2 - 1);
    }

    @Test
    @DisplayName("The ability can be activated repeatedly on the same damaged player")
    void activatesRepeatedly() {
        addReadyAkuba();

        declareAttackers(List.of(0));
        resolveCombat();
        // Mana empties between steps, so pay for the ability after combat damage is dealt.
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(STARTING_LIFE - 2 - 2);
    }

    @Test
    @DisplayName("Cannot target a player Wicked Akuba has not damaged this turn")
    void cannotTargetUndamagedPlayer() {
        addReadyAkuba();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target the controller when only the opponent was damaged")
    void cannotTargetUndamagedController() {
        addReadyAkuba();

        declareAttackers(List.of(0));
        resolveCombat();
        // Mana empties between steps, so pay for the ability after combat damage is dealt.
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage dealt by another creature does not make its victim a legal target")
    void otherCreaturesDamageDoesNotCount() {
        addReadyAkuba();
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveCombat();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
