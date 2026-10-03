package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.c.CoalitionRelic;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvenAugur.class, BlindPhantasm.class, CoalitionRelic.class})
class AvenAugurTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing it during upkeep returns two target creatures")
    void sacrificeDuringUpkeepReturnsTwoCreatures() {
        addCreatureReady(player1, new AvenAugur());
        Permanent firstTarget = addCreatureReady(player2, new BlindPhantasm());
        Permanent secondTarget = addCreatureReady(player2, new BlindPhantasm());
        advanceToUpkeep(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(firstTarget.getId(), secondTarget.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aven Augur");
        harness.assertNotOnBattlefield(player2, "Blind Phantasm");
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Blind Phantasm"))
                .hasSize(2);
    }

    @Test
    @DisplayName("The ability can return one or no target creatures")
    void abilityCanReturnFewerThanTwoCreatures() {
        addCreatureReady(player1, new AvenAugur());
        Permanent target = addCreatureReady(player2, new BlindPhantasm());
        advanceToUpkeep(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Blind Phantasm");
        harness.assertInGraveyard(player1, "Aven Augur");
    }

    @Test
    @DisplayName("The ability may sacrifice Aven Augur without choosing targets")
    void abilityMayChooseNoTargets() {
        addCreatureReady(player1, new AvenAugur());
        advanceToUpkeep(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aven Augur");
    }

    @Test
    @DisplayName("The ability can be activated only during its controller's upkeep")
    void abilityRequiresYourUpkeep() {
        addCreatureReady(player1, new AvenAugur());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");

        harness.assertOnBattlefield(player1, "Aven Augur");
    }

    @Test
    @DisplayName("The ability returns a controlled creature to its owner's hand")
    void abilityReturnsCreatureToItsOwnerHand() {
        addCreatureReady(player1, new AvenAugur());
        BlindPhantasm ownedByPlayer1 = new BlindPhantasm();
        ownedByPlayer1.setOwnerId(player1.getId());
        Permanent target = addCreatureReady(player2, ownedByPlayer1);
        advanceToUpkeep(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Blind Phantasm");
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(card -> card.getName().equals("Blind Phantasm"));
    }

    @Test
    @DisplayName("The ability cannot choose the same creature twice")
    void abilityRequiresDistinctTargets() {
        addCreatureReady(player1, new AvenAugur());
        Permanent target = addCreatureReady(player2, new BlindPhantasm());
        advanceToUpkeep(player1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different");

        harness.assertOnBattlefield(player1, "Aven Augur");
        harness.assertOnBattlefield(player2, "Blind Phantasm");
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void abilityCannotTargetNoncreature() {
        addCreatureReady(player1, new AvenAugur());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new CoalitionRelic());
        advanceToUpkeep(player1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(noncreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Aven Augur");
    }

    @Test
    @DisplayName("Sacrifice is paid immediately and a remaining legal target is returned")
    void canTargetItselfAndAnotherCreature() {
        Permanent augur = addCreatureReady(player1, new AvenAugur());
        Permanent target = addCreatureReady(player2, new BlindPhantasm());
        advanceToUpkeep(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(augur.getId(), target.getId()));

        harness.assertInGraveyard(player1, "Aven Augur");
        harness.assertNotOnBattlefield(player1, "Aven Augur");
        harness.assertOnBattlefield(player2, "Blind Phantasm");

        harness.passBothPriorities();

        harness.assertInHand(player2, "Blind Phantasm");
        harness.assertNotOnBattlefield(player2, "Blind Phantasm");
        harness.assertInGraveyard(player1, "Aven Augur");
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Aven Augur"));
    }

    @Test
    @DisplayName("A tapped creature with summoning sickness can pay the sacrifice cost")
    void tappedSummoningSickAugurCanActivate() {
        advanceToUpkeep(player1);
        Permanent augur = harness.addToBattlefieldAndReturn(player1, new AvenAugur());
        augur.setSummoningSick(true);
        augur.setTapped(true);
        Permanent target = addCreatureReady(player2, new BlindPhantasm());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aven Augur");
        harness.assertInHand(player2, "Blind Phantasm");
    }
}
