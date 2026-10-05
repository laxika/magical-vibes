package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.TerritorialBaloth;
import com.github.laxika.magicalvibes.cards.v.VastwoodGorger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LavaballTrap.class, Mountain.class, Forest.class, GrizzlyBears.class,
        TerritorialBaloth.class, VastwoodGorger.class})
class LavaballTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys two target lands and deals 4 damage to each creature")
    void destroysLandsAndDamagesCreatures() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LavaballTrap()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveInstant(player1, 0, List.of(firstLand.getId(), secondLand.getId()));

        harness.assertInGraveyard(player2, "Mountain");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can be cast for {3}{R}{R} after two opponent lands enter this turn")
    void castsForAlternateCostAfterTwoOpponentLandsEnter() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(),
                List.of(firstLand.getCard(), secondLand.getCard()));
        harness.setHand(player1, List.of(new LavaballTrap()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(firstLand.getId(), secondLand.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mountain");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Alternate cost requires two opponent lands to enter this turn")
    void alternateCostRequiresTwoOpponentLands() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Mountain());
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(), List.of(opponentLand.getCard()));
        harness.setHand(player1, List.of(new LavaballTrap()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(ownLand.getId(), opponentLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a nonland target")
    void rejectsNonlandTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LavaballTrap()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresTwoDistinctLands() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new LavaballTrap()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stillDestroysRemainingLandAndDamagesCreaturesWhenFirstTargetLeaves() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LavaballTrap()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castInstant(player1, 0, List.of(firstLand.getId(), secondLand.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(firstLand);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotDamageCreaturesWhenBothTargetsLeave() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LavaballTrap()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castInstant(player1, 0, List.of(firstLand.getId(), secondLand.getId()));
        gd.playerBattlefields.get(player2.getId()).removeAll(List.of(firstLand, secondLand));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Lavaball Trap");
    }

    @Test
    void ownLandEntriesDoNotEnableAlternateCost() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        gd.permanentsEnteredBattlefieldThisTurn.put(player1.getId(),
                List.of(firstLand.getCard(), secondLand.getCard()));
        gd.permanentsEnteredBattlefieldThisTurn.remove(player2.getId());
        harness.setHand(player1, List.of(new LavaballTrap()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(firstLand.getId(), secondLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void dealsExactlyFourDamageToCreaturesAndNoDamageToPlayers() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player1, new TerritorialBaloth());
        Permanent gorger = harness.addToBattlefieldAndReturn(player2, new VastwoodGorger());
        harness.setHand(player1, List.of(new LavaballTrap()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveInstant(player1, 0, List.of(firstLand.getId(), secondLand.getId()));

        harness.assertInGraveyard(player1, "Territorial Baloth");
        harness.assertOnBattlefield(player2, "Vastwood Gorger");
        assertThat(gorger.getMarkedDamage()).isEqualTo(4);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
