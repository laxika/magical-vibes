package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SpinedSliver;
import com.github.laxika.magicalvibes.cards.s.SpinedWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AcidicSliver.class, SpinedSliver.class, SpinedWurm.class})
class AcidicSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Slivers, including opposing ones, gain the damage ability")
    void grantsAbilityToAllSlivers() {
        Permanent acidicSliver = addCreatureReady(player1, new AcidicSliver());
        Permanent ownSliver = addCreatureReady(player1, new SpinedSliver());
        Permanent opposingSliver = addCreatureReady(player2, new SpinedSliver());

        assertThat(gs.getEffectiveActivatedAbilities(gd, acidicSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, ownSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, opposingSliver)).hasSize(1);
    }

    @Test
    @DisplayName("Activating the granted ability sacrifices the Sliver and deals 2 damage to a player")
    void sacrificesSliverAndDamagesPlayer() {
        addCreatureReady(player1, new AcidicSliver());
        Permanent ownSliver = addCreatureReady(player1, new SpinedSliver());
        harness.setLife(player2, 20);

        activateGrantedAbility(gd.playerBattlefields.get(player1.getId()).indexOf(ownSliver), player2.getId());

        harness.assertNotOnBattlefield(player1, "Spined Sliver");
        harness.assertInGraveyard(player1, "Spined Sliver");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Activating the granted ability deals exactly 2 damage to a creature")
    void damagesCreature() {
        addCreatureReady(player1, new AcidicSliver());
        Permanent ownSliver = addCreatureReady(player1, new SpinedSliver());
        Permanent target = addCreatureReady(player2, new SpinedWurm());

        activateGrantedAbility(gd.playerBattlefields.get(player1.getId()).indexOf(ownSliver), target.getId());

        harness.assertNotOnBattlefield(player1, "Spined Sliver");
        harness.assertInGraveyard(player1, "Spined Sliver");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Spined Wurm");
    }

    @Test
    @DisplayName("Non-Sliver creatures do not gain the ability")
    void doesNotGrantAbilityToNonSlivers() {
        addCreatureReady(player1, new AcidicSliver());
        Permanent wurm = addCreatureReady(player1, new SpinedWurm());

        assertThat(gs.getEffectiveActivatedAbilities(gd, wurm)).isEmpty();
    }

    @Test
    @DisplayName("Acidic Sliver can activate its own granted ability")
    void grantsAbilityToItself() {
        addCreatureReady(player1, new AcidicSliver());
        harness.setLife(player2, 20);

        activateGrantedAbility(0, player2.getId());

        harness.assertNotOnBattlefield(player1, "Acidic Sliver");
        harness.assertInGraveyard(player1, "Acidic Sliver");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Slivers lose Acidic Sliver's granted ability when it leaves the battlefield")
    void losesGrantedAbilityWhenSourceLeaves() {
        Permanent source = addCreatureReady(player1, new AcidicSliver());
        Permanent otherSliver = addCreatureReady(player1, new SpinedSliver());
        gd.playerBattlefields.get(player1.getId()).remove(source);

        assertThat(gs.getEffectiveActivatedAbilities(gd, otherSliver)).isEmpty();
    }

    @Test
    @DisplayName("An activated ability resolves after Acidic Sliver leaves the battlefield")
    void activatedAbilityResolvesAfterGrantingSourceLeaves() {
        Permanent source = addCreatureReady(player1, new AcidicSliver());
        Permanent otherSliver = addCreatureReady(player1, new SpinedSliver());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(otherSliver), 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Spined Sliver");
    }

    @Test
    @DisplayName("An opponent can sacrifice their Sliver using Acidic Sliver's ability")
    void opponentCanActivateGrantedAbility() {
        addCreatureReady(player1, new AcidicSliver());
        addCreatureReady(player2, new SpinedSliver());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Spined Sliver");
        harness.assertNotOnBattlefield(player2, "Spined Sliver");
        harness.assertOnBattlefield(player1, "Acidic Sliver");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("A tapped Sliver with summoning sickness can activate the ability")
    void tappedSummoningSickSliverCanActivate() {
        addCreatureReady(player1, new AcidicSliver());
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, new SpinedSliver());
        sliver.setSummoningSick(true);
        sliver.setTapped(true);
        harness.setLife(player2, 20);

        activateGrantedAbility(1, player2.getId());

        harness.assertInGraveyard(player1, "Spined Sliver");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Mana and sacrifice are paid before the damage ability resolves")
    void paysCostsBeforeResolution() {
        addCreatureReady(player1, new AcidicSliver());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Acidic Sliver");
        harness.assertInGraveyard(player1, "Acidic Sliver");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A departing target prevents damage without refunding the sacrifice")
    void targetLeavingDoesNotRefundCost() {
        addCreatureReady(player1, new AcidicSliver());
        Permanent target = addCreatureReady(player2, new SpinedWurm());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Acidic Sliver");
        harness.assertNotOnBattlefield(player1, "Acidic Sliver");
    }

    private void activateGrantedAbility(int permanentIndex, UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, permanentIndex, 0, null, targetId);
        harness.passBothPriorities();
    }
}
