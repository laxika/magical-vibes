package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.e.ExperimentalFrenzy;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrnOfGodfire.class, GrizzlyBears.class, ExperimentalFrenzy.class, Forest.class})
class UrnOfGodfireTest extends BaseCardTest {

    @Test
    @DisplayName("The mana ability adds one mana of the chosen color")
    void addsManaOfChosenColor() {
        harness.addToBattlefield(player1, new UrnOfGodfire());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The sacrifice ability destroys a target creature")
    void destroysTargetCreature() {
        Permanent urn = harness.addToBattlefieldAndReturn(player1, new UrnOfGodfire());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, bears.getId());

        harness.assertNotOnBattlefield(player1, "Urn of Godfire");
        assertThat(urn.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Urn of Godfire");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The sacrifice ability destroys a target enchantment")
    void destroysTargetEnchantment() {
        harness.addToBattlefieldAndReturn(player1, new UrnOfGodfire());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new ExperimentalFrenzy());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 1, null, enchantment.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Experimental Frenzy");
    }

    @Test
    @DisplayName("The sacrifice ability cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefieldAndReturn(player1, new UrnOfGodfire());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The mana ability resolves immediately and can be activated repeatedly without tapping")
    void manaAbilityCanBeRepeatedWithoutTapping() {
        Permanent urn = harness.addToBattlefieldAndReturn(player1, new UrnOfGodfire());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(urn.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Urn of Godfire");
    }

    @Test
    @DisplayName("A tapped Urn can still activate its mana ability")
    void tappedUrnCanProduceMana() {
        Permanent urn = harness.addToBattlefieldAndReturn(player1, new UrnOfGodfire());
        urn.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(urn.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The mana ability requires two mana")
    void cannotProduceManaWithInsufficientMana() {
        harness.addToBattlefield(player1, new UrnOfGodfire());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Urn of Godfire");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Urn cannot activate its destruction ability")
    void tappedUrnCannotDestroy() {
        Permanent urn = harness.addToBattlefieldAndReturn(player1, new UrnOfGodfire());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        urn.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Urn of Godfire");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(6);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The destruction ability requires six mana without sacrificing the Urn on failure")
    void cannotDestroyWithInsufficientMana() {
        Permanent urn = harness.addToBattlefieldAndReturn(player1, new UrnOfGodfire());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Urn of Godfire");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(urn.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }
}
