package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.StoneworkPuma;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhiplashTrap.class, StoneworkPuma.class})
class WhiplashTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Returns two target creatures to their owners' hands")
    void returnsTwoTargetCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new StoneworkPuma());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new StoneworkPuma());
        harness.setHand(player1, List.of(new WhiplashTrap()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));

        harness.assertInHand(player1, "Stonework Puma");
        harness.assertInHand(player2, "Stonework Puma");
    }

    @Test
    @DisplayName("Can be cast for {U} after two opponent creatures entered this turn")
    void castsForAlternateCostAfterTwoOpponentCreaturesEntered() {
        Permanent firstCreature = harness.enterBattlefieldAndReturn(player2, new StoneworkPuma());
        Permanent secondCreature = harness.enterBattlefieldAndReturn(player2, new StoneworkPuma());
        harness.setHand(player1, List.of(new WhiplashTrap()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(firstCreature.getId(), secondCreature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Stonework Puma");
        harness.assertInGraveyard(player1, "Whiplash Trap");
    }

    @Test
    @DisplayName("Alternate cost requires two creatures to have entered under an opponent's control")
    void alternateCostRequiresTwoOpponentCreaturesToHaveEntered() {
        Permanent firstCreature = harness.enterBattlefieldAndReturn(player2, new StoneworkPuma());
        Permanent secondCreature = harness.enterBattlefieldAndReturn(player1, new StoneworkPuma());
        harness.setHand(player1, List.of(new WhiplashTrap()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires two distinct creature targets")
    void rejectsDuplicateCreatureTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new StoneworkPuma());
        harness.setHand(player1, List.of(new WhiplashTrap()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be cast with only one creature target")
    void requiresTwoCreatureTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new StoneworkPuma());
        harness.setHand(player1, List.of(new WhiplashTrap()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns the remaining legal target when the other target leaves")
    void returnsRemainingLegalTarget() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new StoneworkPuma());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new StoneworkPuma());
        harness.setHand(player1, List.of(new WhiplashTrap()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, List.of(firstCreature.getId(), secondCreature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(firstCreature);
        gd.addCardToHand(player2.getId(), firstCreature.getCard());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Stonework Puma");
        harness.assertInHand(player1, "Stonework Puma");
        harness.assertInGraveyard(player1, "Whiplash Trap");
    }

    @Test
    @DisplayName("Returns a creature to its owner even when an opponent controls it")
    void returnsStolenCreatureToOwner() {
        Permanent stolenCreature = harness.addToBattlefieldAndReturn(player2, new StoneworkPuma());
        gd.stolenCreatures.put(stolenCreature.getId(), player1.getId());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new StoneworkPuma());
        harness.setHand(player1, List.of(new WhiplashTrap()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0,
                List.of(stolenCreature.getId(), otherCreature.getId()));

        harness.assertInHand(player1, "Stonework Puma");
        harness.assertInHand(player2, "Stonework Puma");
        harness.assertNotOnBattlefield(player2, "Stonework Puma");
    }

    @Test
    @DisplayName("Alternate cost still applies when an entering creature has left the battlefield")
    void alternateCostCountsCreaturesThatAlreadyLeft() {
        Permanent departedCreature = harness.enterBattlefieldAndReturn(player2, new StoneworkPuma());
        Permanent opposingCreature = harness.enterBattlefieldAndReturn(player2, new StoneworkPuma());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new StoneworkPuma());
        gd.playerBattlefields.get(player2.getId()).remove(departedCreature);
        gd.addCardToHand(player2.getId(), departedCreature.getCard());
        harness.setHand(player1, List.of(new WhiplashTrap()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(opposingCreature.getId(), ownCreature.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Stonework Puma");
        harness.assertNotOnBattlefield(player2, "Stonework Puma");
        harness.assertInHand(player1, "Stonework Puma");
        harness.assertInGraveyard(player1, "Whiplash Trap");
    }
}
