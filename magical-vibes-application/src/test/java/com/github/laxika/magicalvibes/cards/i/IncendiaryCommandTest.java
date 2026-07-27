package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IncendiaryCommandTest extends BaseCardTest {

    // Mode indices: 0 = 4 damage to player/planeswalker, 1 = 2 damage to each creature,
    //               2 = destroy nonbasic land, 3 = each player wheels their hand.

    @Test
    @DisplayName("Damage-player + damage-each-creature: burns a player and wipes small creatures")
    void damagePlayerAndDamageEachCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IncendiaryCommand()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 1}, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Damage-planeswalker + destroy-land: two target slots bind to the right effects")
    void damagePlaneswalkerAndDestroyLand() {
        Permanent chandra = new Permanent(new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);
        gd.playerBattlefields.get(player2.getId()).add(chandra);
        harness.addToBattlefield(player2, new GhostQuarter());
        java.util.UUID ghostQuarterId = harness.getPermanentId(player2, "Ghost Quarter");

        harness.setHand(player1, List.of(new IncendiaryCommand()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 2},
                List.of(chandra.getId(), ghostQuarterId));
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2); // 6 - 4
        harness.assertNotOnBattlefield(player2, "Ghost Quarter");
    }

    @Test
    @DisplayName("Destroy-land + each-player-wheel: destroys the land and both players wheel their hands")
    void destroyLandAndEachPlayerWheels() {
        harness.addToBattlefield(player2, new GhostQuarter());
        java.util.UUID ghostQuarterId = harness.getPermanentId(player2, "Ghost Quarter");

        harness.setHand(player1, List.of(new IncendiaryCommand(), new GrizzlyBears(), new HillGiant()));
        harness.setLibrary(player1, List.of(new Plains(), new Island()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new Spellbook()));
        harness.setLibrary(player2, List.of(new Forest()));

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{2, 3}, List.of(ghostQuarterId));
        harness.passBothPriorities();

        // Land destroyed
        harness.assertNotOnBattlefield(player2, "Ghost Quarter");

        // Player 1 discarded their two remaining cards and drew two from library
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInHand(player1, "Plains");
        harness.assertInHand(player1, "Island");

        // Player 2 discarded their one card and drew one from library
        harness.assertInGraveyard(player2, "Spellbook");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Destroy-land mode targeting a basic land is rejected")
    void destroyLandRejectsBasicLand() {
        harness.addToBattlefield(player2, new Mountain());
        java.util.UUID mountainId = harness.getPermanentId(player2, "Mountain");

        harness.setHand(player1, List.of(new IncendiaryCommand()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() ->
                harness.castModalSorceryWithModes(player1, 0, 2, new int[]{2, 3}, List.of(mountainId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
