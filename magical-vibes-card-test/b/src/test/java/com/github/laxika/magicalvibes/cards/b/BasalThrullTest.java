package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BasalThrull.class)
class BasalThrullTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping and sacrificing Basal Thrull adds two black mana")
    void tapsAndSacrificesForTwoBlackMana() {
        addCreatureReady(player1, new BasalThrull());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Basal Thrull");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate Basal Thrull while tapped")
    void cannotActivateWhileTapped() {
        Permanent basalThrull = addCreatureReady(player1, new BasalThrull());
        basalThrull.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.assertOnBattlefield(player1, "Basal Thrull");
    }

    @Test
    @DisplayName("Summoning sickness prevents activation without paying any costs")
    void cannotActivateWhileSummoningSick() {
        Permanent basalThrull = harness.addToBattlefieldAndReturn(player1, new BasalThrull());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(basalThrull.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.assertOnBattlefield(player1, "Basal Thrull");
        harness.assertNotInGraveyard(player1, "Basal Thrull");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The second player's Basal Thrull produces mana for that player")
    void producesManaForItsController() {
        addCreatureReady(player2, new BasalThrull());

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.assertNotOnBattlefield(player2, "Basal Thrull");
        harness.assertInGraveyard(player2, "Basal Thrull");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Activating one Basal Thrull sacrifices only that creature")
    void sacrificesOnlyTheActivatedThrull() {
        Permanent first = addCreatureReady(player1, new BasalThrull());
        Permanent second = addCreatureReady(player1, new BasalThrull());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(second).doesNotContain(first);
        assertThat(second.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Basal Thrull");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }
}
