package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PyroclasticElemental.class, GreenwoodSentinel.class})
class PyroclasticElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 1 damage to target player")
    void dealsOneDamageToTargetPlayer() {
        harness.setLife(player2, 20);
        addReadyElemental(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Ability can target its controller")
    void canTargetController() {
        harness.setLife(player1, 20);
        addReadyElemental(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly")
    void canActivateRepeatedly() {
        harness.setLife(player2, 20);
        addReadyElemental(player1);
        harness.addMana(player1, ManaColor.RED, 6);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Ability cannot target a creature")
    void cannotTargetCreature() {
        addReadyElemental(player1);
        harness.addMana(player1, ManaColor.RED, 3);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player");
    }

    @Test
    @DisplayName("Ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        harness.setLife(player2, 20);
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new PyroclasticElemental());
        elemental.tap();
        elemental.setSummoningSick(true);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(elemental.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Generic activation cost can be paid with another color")
    void canPayGenericCostWithAnotherColor() {
        harness.setLife(player2, 20);
        addReadyElemental(player1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability requires two red mana")
    void cannotActivateWithOnlyOneRedMana() {
        addReadyElemental(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability requires the generic mana in addition to two red")
    void cannotActivateWithOnlyTwoMana() {
        addReadyElemental(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private void addReadyElemental(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new PyroclasticElemental());
        perm.setSummoningSick(false);
    }
}
