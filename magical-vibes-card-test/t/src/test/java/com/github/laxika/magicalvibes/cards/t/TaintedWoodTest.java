package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Swamp;
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

@CardUsed({TaintedWood.class, Swamp.class})
class TaintedWoodTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana produces one colorless")
    void tappingForColorlessMana() {
        Permanent wood = addCreatureReady(player1, new TaintedWood());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(wood.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Colored mana ability requires a Swamp")
    void coloredManaRequiresSwamp() {
        Permanent wood = addCreatureReady(player1, new TaintedWood());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("a Swamp");
        assertThat(wood.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Colored mana ability can produce black mana")
    void coloredManaCanProduceBlack() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent wood = addCreatureReady(player1, new TaintedWood());

        harness.activateAbility(player1, 1, 1, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(wood.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Colored mana ability can produce green mana")
    void coloredManaCanProduceGreen() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent wood = addCreatureReady(player1, new TaintedWood());

        harness.activateAbility(player1, 1, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(wood.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent's Swamp does not enable colored mana")
    void opponentsSwampDoesNotEnableColoredMana() {
        harness.addToBattlefield(player2, new Swamp());
        Permanent wood = addCreatureReady(player1, new TaintedWood());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wood.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Swamp still enables colored mana")
    void tappedSwampEnablesColoredMana() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        swamp.tap();
        Permanent wood = harness.addToBattlefieldAndReturn(player1, new TaintedWood());

        harness.activateAbility(player1, 1, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(wood.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing the last Swamp disables colored mana but leaves colorless available")
    void losingLastSwampDisablesOnlyColoredMana() {
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Permanent wood = harness.addToBattlefieldAndReturn(player1, new TaintedWood());
        gd.playerBattlefields.get(player1.getId()).remove(swamp);
        gd.playerGraveyards.get(player1.getId()).add(swamp.getCard());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("a Swamp");
        assertThat(wood.isTapped()).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(wood.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tainted Wood can tap for mana the turn it is played")
    void canTapForManaImmediatelyAfterBeingPlayed() {
        harness.setHand(player1, List.of(new TaintedWood()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
