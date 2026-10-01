package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.b.BonesplitterSliver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PsionicSliver.class, BonesplitterSliver.class, AshcoatBear.class})
class PsionicSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Slivers, including opposing ones, gain the damage ability")
    void grantsAbilityToAllSlivers() {
        Permanent psionicSliver = addCreatureReady(player1, new PsionicSliver());
        Permanent ownSliver = addCreatureReady(player1, new BonesplitterSliver());
        Permanent opposingSliver = addCreatureReady(player2, new BonesplitterSliver());

        assertThat(gs.getEffectiveActivatedAbilities(gd, psionicSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, ownSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, opposingSliver)).hasSize(1);
    }

    @Test
    @DisplayName("An opposing Sliver can activate the granted ability")
    void opposingSliverCanActivateAbility() {
        addCreatureReady(player1, new PsionicSliver());
        addCreatureReady(player2, new BonesplitterSliver());
        harness.setLife(player1, 20);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertNotOnBattlefield(player2, "Bonesplitter Sliver");
        harness.assertInGraveyard(player2, "Bonesplitter Sliver");
    }

    @Test
    @DisplayName("The granted ability deals 2 damage to a creature and 3 damage to the Sliver")
    void damagesTargetCreatureAndSourceSliver() {
        addCreatureReady(player1, new PsionicSliver());
        Permanent sourceSliver = addCreatureReady(player1, new BonesplitterSliver());
        Permanent target = addCreatureReady(player2, new AshcoatBear());

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ashcoat Bear");
        assertThat(sourceSliver.getMarkedDamage()).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Bonesplitter Sliver");
        harness.assertInGraveyard(player1, "Bonesplitter Sliver");
    }

    @Test
    @DisplayName("A 2/2 Psionic Sliver dies from its own ability")
    void sourceSliverDiesFromSelfDamage() {
        addCreatureReady(player1, new PsionicSliver());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player1, "Psionic Sliver");
        harness.assertInGraveyard(player1, "Psionic Sliver");
    }

    @Test
    @DisplayName("Non-Sliver creatures do not gain the ability")
    void doesNotGrantAbilityToNonSlivers() {
        addCreatureReady(player1, new PsionicSliver());
        addCreatureReady(player1, new AshcoatBear());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
