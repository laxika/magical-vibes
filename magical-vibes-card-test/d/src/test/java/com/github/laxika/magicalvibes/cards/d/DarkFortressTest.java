package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarkFortress.class, Swamp.class})
class DarkFortressTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Dark Fortress produces colorless mana")
    void tappingProducesColorlessMana() {
        Permanent fortress = addReadyFortress();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(fortress.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Black or red mana requires a newly entered Dark Fortress or a basic land")
    void coloredManaRequiresCondition() {
        Permanent fortress = addReadyFortress();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entered this turn or if you control a basic land");
        assertThat(fortress.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A Dark Fortress that entered this turn can produce black or red mana")
    void newlyEnteredFortressProducesChosenMana() {
        Permanent fortress = harness.enterBattlefieldAndReturn(player1, new DarkFortress());

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(fortress.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A basic land enables Dark Fortress to produce black or red mana")
    void basicLandEnablesChosenMana() {
        Permanent fortress = addReadyFortress();
        harness.addToBattlefield(player1, new Swamp());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(fortress.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's basic land does not enable colored mana")
    void opponentsBasicLandDoesNotEnableColoredMana() {
        Permanent fortress = addReadyFortress();
        harness.addToBattlefield(player2, new Swamp());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entered this turn or if you control a basic land");
        assertThat(fortress.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Another Dark Fortress entering this turn does not enable an older Fortress")
    void anotherNewFortressDoesNotEnableOlderFortress() {
        Permanent fortress = addReadyFortress();
        harness.enterBattlefieldAndReturn(player1, new DarkFortress());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entered this turn or if you control a basic land");
        assertThat(fortress.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Losing the last basic land disables colored mana but leaves colorless mana available")
    void losingBasicLandDisablesOnlyColoredMana() {
        Permanent fortress = addReadyFortress();
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");
        fortress.untap();
        gd.playerBattlefields.get(player1.getId()).remove(swamp);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entered this turn or if you control a basic land");
        assertThat(fortress.isTapped()).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(fortress.isTapped()).isTrue();
    }

    private Permanent addReadyFortress() {
        return addCreatureReady(player1, new DarkFortress());
    }
}
