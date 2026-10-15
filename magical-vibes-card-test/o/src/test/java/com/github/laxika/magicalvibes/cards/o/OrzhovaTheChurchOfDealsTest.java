package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrzhovaTheChurchOfDeals.class})
class OrzhovaTheChurchOfDealsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tapAbilityAddsColorlessMana() {
        Permanent orzhova = harness.addToBattlefieldAndReturn(player1, new OrzhovaTheChurchOfDeals());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(orzhova.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Pays mana to make a player lose life and gain 1 life")
    void lifeAbilityDrainsTargetPlayer() {
        Permanent orzhova = harness.addToBattlefieldAndReturn(player1, new OrzhovaTheChurchOfDeals());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(orzhova.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target its controller")
    void lifeAbilityCanTargetController() {
        Permanent orzhova = harness.addToBattlefieldAndReturn(player1, new OrzhovaTheChurchOfDeals());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(orzhova.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Rejects a permanent as the target")
    void lifeAbilityRequiresAPlayerTarget() {
        harness.addToBattlefield(player1, new OrzhovaTheChurchOfDeals());
        Permanent orzhova = harness.addToBattlefieldAndReturn(player2, new OrzhovaTheChurchOfDeals());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, orzhova.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("player");
    }

    @Test
    @DisplayName("Life changes wait for resolution and the ability survives removal of its source")
    void lifeAbilityUsesStackAndSurvivesSourceRemoval() {
        Permanent orzhova = harness.addToBattlefieldAndReturn(player1, new OrzhovaTheChurchOfDeals());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(orzhova.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, orzhova));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(orzhova);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A tapped land cannot activate either ability")
    void tappedLandCannotActivate() {
        Permanent orzhova = harness.addToBattlefieldAndReturn(player1, new OrzhovaTheChurchOfDeals());
        orzhova.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Colorless mana cannot replace the white and black activation costs")
    void lifeAbilityRequiresColoredMana() {
        Permanent orzhova = harness.addToBattlefieldAndReturn(player1, new OrzhovaTheChurchOfDeals());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(orzhova.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(5);
    }
}
