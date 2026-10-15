package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlightedCataract.class, GrizzlyBears.class})
class BlightedCataractTest extends BaseCardTest {

    @Test
    @DisplayName("Blighted Cataract taps for colorless mana")
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new BlightedCataract());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Pays six mana, draws two cards, and sacrifices itself")
    void drawsTwoCardsAndSacrificesItself() {
        harness.addToBattlefield(player1, new BlightedCataract());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertInGraveyard(player1, "Blighted Cataract");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    void manaAbilityTapsImmediatelyAndDoesNotUseTheStack() {
        harness.addToBattlefield(player1, new BlightedCataract());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("tapped");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void cannotDrawAfterTappingForMana() {
        harness.addToBattlefield(player1, new BlightedCataract());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("tapped");

        harness.assertOnBattlefield(player1, "Blighted Cataract");
        harness.assertNotInGraveyard(player1, "Blighted Cataract");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayBlueRequirementWithOnlyColorlessMana() {
        harness.addToBattlefield(player1, new BlightedCataract());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("mana");

        harness.assertOnBattlefield(player1, "Blighted Cataract");
        harness.assertNotInGraveyard(player1, "Blighted Cataract");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(6);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithOnlyFiveManaIncludingBlue() {
        harness.addToBattlefield(player1, new BlightedCataract());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("mana");

        harness.assertOnBattlefield(player1, "Blighted Cataract");
        harness.assertNotInGraveyard(player1, "Blighted Cataract");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void drawsForControllerOnlyAfterResolutionAndPaysAllCostsImmediately() {
        harness.addToBattlefield(player2, new BlightedCataract());
        harness.setLibrary(player2, List.of(new BlightedCataract(), new BlightedCataract()));
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.addMana(player2, ManaColor.BLUE, 1);
        int controllerHandBefore = gd.playerHands.get(player2.getId()).size();
        int opponentHandBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player2, 0, 1, null, null);

        harness.assertNotOnBattlefield(player2, "Blighted Cataract");
        harness.assertInGraveyard(player2, "Blighted Cataract");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(controllerHandBefore);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(controllerHandBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(opponentHandBefore);
    }
}
