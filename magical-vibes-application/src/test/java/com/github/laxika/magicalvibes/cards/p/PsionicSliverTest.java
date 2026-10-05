package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.b.BonesplitterSliver;
import com.github.laxika.magicalvibes.cards.s.Snapback;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PsionicSliver.class, BonesplitterSliver.class, AshcoatBear.class, Snapback.class})
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

    @Test
    void canTargetItselfAndReceivesAllFiveDamage() {
        Permanent source = addCreatureReady(player1, new PsionicSliver());

        harness.activateAbility(player1, 0, null, source.getId());
        harness.passBothPriorities();

        assertThat(source.getMarkedDamage()).isEqualTo(5);
        harness.assertInGraveyard(player1, "Psionic Sliver");
    }

    @Test
    void illegalOnlyTargetPreventsSelfDamageAsWell() {
        Permanent source = addCreatureReady(player1, new PsionicSliver());
        Permanent target = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player2, List.of(new Snapback()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Ashcoat Bear");
        harness.assertOnBattlefield(player1, "Psionic Sliver");
        assertThat(source.getMarkedDamage()).isZero();
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    void abilityStillDealsDamageAfterSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new PsionicSliver());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Snapback()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, source.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInHand(player1, "Psionic Sliver");
        harness.assertNotInGraveyard(player1, "Psionic Sliver");
    }
}
