package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScuttlingSliver.class, GrizzlyBears.class, SyphonSliver.class})
class ScuttlingSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Scuttling Sliver can pay {2} to untap itself")
    void grantsAbilityToItself() {
        Permanent scuttlingSliver = harness.addToBattlefieldAndReturn(player1, new ScuttlingSliver());
        scuttlingSliver.setSummoningSick(false);
        scuttlingSliver.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(scuttlingSliver.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Scuttling Sliver grants the untap ability to other Slivers you control")
    void grantsAbilityToOtherSlivers() {
        harness.addToBattlefield(player1, new ScuttlingSliver());
        Permanent syphonSliver = harness.addToBattlefieldAndReturn(player1, new SyphonSliver());
        syphonSliver.setSummoningSick(false);
        syphonSliver.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(syphonSliver.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Scuttling Sliver does not grant the ability to non-Slivers")
    void doesNotGrantAbilityToNonSlivers() {
        harness.addToBattlefield(player1, new ScuttlingSliver());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Scuttling Sliver does not grant the ability to an opponent's Slivers")
    void doesNotGrantAbilityToOpponentSlivers() {
        harness.addToBattlefield(player1, new ScuttlingSliver());
        harness.addToBattlefield(player2, new SyphonSliver());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("A summoning-sick Sliver can activate the untap ability")
    void summoningSicknessDoesNotPreventActivation() {
        Permanent scuttling = harness.addToBattlefieldAndReturn(player1, new ScuttlingSliver());
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, new SyphonSliver());
        scuttling.tap();
        sliver.setSummoningSick(true);
        sliver.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);

        assertThat(sliver.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(sliver.isTapped()).isFalse();
        assertThat(scuttling.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The untap ability requires two mana")
    void cannotActivateWithOnlyOneMana() {
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, new ScuttlingSliver());
        sliver.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(sliver.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Colored mana can pay the generic untap cost")
    void coloredManaPaysGenericCost() {
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, new ScuttlingSliver());
        sliver.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sliver.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An untapped Sliver can activate the untap ability")
    void canActivateWhileAlreadyUntapped() {
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, new ScuttlingSliver());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(sliver.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A granted untap ability resolves after Scuttling Sliver leaves the battlefield")
    void activatedAbilitySurvivesGrantingSourceLeaving() {
        Permanent scuttling = harness.addToBattlefieldAndReturn(player1, new ScuttlingSliver());
        Permanent syphon = harness.addToBattlefieldAndReturn(player1, new SyphonSliver());
        scuttling.tap();
        syphon.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(scuttling);
        gd.playerGraveyards.get(player1.getId()).add(scuttling.getCard());
        harness.passBothPriorities();

        assertThat(syphon.isTapped()).isFalse();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }
}
