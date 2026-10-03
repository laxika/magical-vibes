package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SidewinderSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BasalSliver.class, SidewinderSliver.class, BenalishCavalry.class})
class BasalSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Basal Sliver grants its sacrifice mana ability to itself")
    void grantsAbilityToItself() {
        Permanent basalSliver = addCreatureReady(player1, new BasalSliver());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(basalSliver);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(basalSliver.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Basal Sliver's ability does not require tapping the Sliver")
    void abilityDoesNotRequireTap() {
        Permanent basalSliver = addCreatureReady(player1, new BasalSliver());
        basalSliver.tap();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(basalSliver.getCard());
    }

    @Test
    @DisplayName("All other Slivers gain Basal Sliver's sacrifice mana ability")
    void grantsAbilityToOtherSlivers() {
        harness.addToBattlefield(player1, new BasalSliver());
        Permanent otherSliver = addCreatureReady(player1, new SidewinderSliver());

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(otherSliver);
    }

    @Test
    @DisplayName("Basal Sliver grants the ability to opposing Slivers")
    void grantsAbilityToOpposingSlivers() {
        harness.addToBattlefield(player1, new BasalSliver());
        Permanent opposingSliver = addCreatureReady(player2, new SidewinderSliver());

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingSliver);
    }

    @Test
    @DisplayName("Non-Sliver creatures do not gain the sacrifice mana ability")
    void doesNotGrantAbilityToNonSlivers() {
        harness.addToBattlefield(player1, new BasalSliver());
        addCreatureReady(player1, new BenalishCavalry());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A newly entered Basal Sliver can sacrifice itself for mana")
    void canSacrificeItselfWithSummoningSickness() {
        Permanent basalSliver = harness.enterBattlefieldAndReturn(player1, new BasalSliver());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(basalSliver);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(basalSliver.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A newly entered opposing Sliver can use the granted mana ability")
    void opposingSliverCanActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new BasalSliver());
        Permanent opposingSliver = harness.enterBattlefieldAndReturn(player2, new SidewinderSliver());

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingSliver);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingSliver.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Other Slivers lose the granted ability after Basal Sliver sacrifices itself")
    void grantedAbilityDisappearsWhenBasalSliverLeaves() {
        addCreatureReady(player1, new BasalSliver());
        Permanent otherSliver = addCreatureReady(player1, new SidewinderSliver());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherSliver);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

}
