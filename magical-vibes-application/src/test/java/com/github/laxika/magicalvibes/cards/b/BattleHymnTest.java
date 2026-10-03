package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.v.Vanishment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattleHymn.class, GrizzlyBears.class, Mountain.class, Vanishment.class})
class BattleHymnTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one red mana for each creature the controller controls")
    void addsRedManaPerControlledCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        castBattleHymn();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Adds no mana when the controller controls no creatures")
    void addsNoManaWithoutCreatures() {
        addCreatureReady(player2, new GrizzlyBears());

        castBattleHymn();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        addCreatureReady(player1, new GrizzlyBears());

        castBattleHymn();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Battle Hymn");
    }

    @Test
    @DisplayName("Counts tapped creatures but not noncreature permanents")
    void countsTappedCreaturesButNotLands() {
        addCreatureReady(player1, new GrizzlyBears()).setTapped(true);
        harness.addToBattlefield(player1, new Mountain());

        castBattleHymn();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts creatures at resolution after a creature leaves in response")
    void countsCreaturesAtResolution() {
        var creature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        castBattleHymn();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.setHand(player2, List.of(new Vanishment()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void castBattleHymn() {
        harness.castFromHand(player1, new BattleHymn(), "{1}{R}");
    }
}
