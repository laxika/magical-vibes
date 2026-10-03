package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarrelsOfBlastingJelly.class, GrizzlyBears.class, Badgermole.class})
class BarrelsOfBlastingJellyTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds one mana of the chosen color and can be activated only once each turn")
    void manaAbilityAddsManaOnlyOnceEachTurn() {
        Permanent jelly = harness.addToBattlefieldAndReturn(player1, new BarrelsOfBlastingJelly());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);

        jelly.untap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Sacrifices itself and deals 5 damage to target creature")
    void sacrificesItselfAndDealsDamageToTargetCreature() {
        harness.addToBattlefield(player1, new BarrelsOfBlastingJelly());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, target.getId());

        harness.assertInGraveyard(player1, "Barrels of Blasting Jelly");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void manaAbilityWorksWhileTappedAndResolvesWithoutUsingTheStack() {
        Permanent jelly = harness.addToBattlefieldAndReturn(player1, new BarrelsOfBlastingJelly());
        jelly.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.stack).isEmpty();
        assertThat(jelly.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void manaAbilityCanBeUsedAgainDuringTheOpponentsTurn() {
        harness.addToBattlefield(player1, new BarrelsOfBlastingJelly());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void eachPermanentHasItsOwnManaActivationLimit() {
        harness.addToBattlefield(player1, new BarrelsOfBlastingJelly());
        harness.addToBattlefield(player1, new BarrelsOfBlastingJelly());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    void manaAbilityRequiresPayment() {
        harness.addToBattlefield(player1, new BarrelsOfBlastingJelly());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void manaActivationDoesNotPreventDamageAbilityAndOwnCreatureIsLegal() {
        Permanent jelly = harness.addToBattlefieldAndReturn(player1, new BarrelsOfBlastingJelly());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Badgermole());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        assertThat(jelly.isTapped()).isFalse();

        harness.activateAbility(player1, 0, 1, null, target.getId());

        harness.assertInGraveyard(player1, "Barrels of Blasting Jelly");
        assertThat(target.getMarkedDamage()).isZero();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
        harness.assertInGraveyard(player1, "Badgermole");
    }

    @Test
    void damageAbilityCannotTargetAPlayer() {
        harness.addToBattlefield(player1, new BarrelsOfBlastingJelly());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Barrels of Blasting Jelly");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(5);
    }

    @Test
    void damageAbilityRequiresAnUntappedSource() {
        Permanent jelly = harness.addToBattlefieldAndReturn(player1, new BarrelsOfBlastingJelly());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Badgermole());
        jelly.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        harness.assertOnBattlefield(player1, "Barrels of Blasting Jelly");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(5);
    }

    @Test
    void damageAbilityRequiresFiveManaBeforeSacrificing() {
        Permanent jelly = harness.addToBattlefieldAndReturn(player1, new BarrelsOfBlastingJelly());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Badgermole());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertOnBattlefield(player1, "Barrels of Blasting Jelly");
        assertThat(jelly.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
    }

    @Test
    void damageAbilityCannotTargetANoncreatureArtifact() {
        harness.addToBattlefield(player1, new BarrelsOfBlastingJelly());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BarrelsOfBlastingJelly());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Barrels of Blasting Jelly");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(5);
    }
}
