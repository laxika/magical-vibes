package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.NotionThief;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IncendiaryCommand.class, ChandraNalaar.class, GhostQuarter.class,
        GrizzlyBears.class, HillGiant.class, Forest.class, Mountain.class,
        Plains.class, Island.class, Spellbook.class, NotionThief.class})
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

        harness.assertLife(player2, 16);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Damage-player mode can target the controller")
    void damagePlayerModeCanDamageController() {
        harness.setHand(player1, List.of(new IncendiaryCommand()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castModalSorceryWithModes(
                player1, 0, 2, new int[]{0, 1}, List.of(player1.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Damage-planeswalker + destroy-land: two target slots bind to the right effects")
    void damagePlaneswalkerAndDestroyLand() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);
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

    @Test
    @DisplayName("Both hands are discarded before any draws redirected by Notion Thief")
    void discardBothHandsBeforeDrawing() {
        harness.addToBattlefield(player2, new NotionThief());
        harness.setHand(player1, List.of(new IncendiaryCommand(), new Mountain()));
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setLibrary(player2, List.of(new Plains(), new Island(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 3}, List.of(player2.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertInHand(player2, "Plains");
        harness.assertInHand(player2, "Island");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName()).containsExactly("Forest");
    }

    @Test
    @DisplayName("The untargeted modes work with empty hands and damage only creatures")
    void untargetedModesWithEmptyHands() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IncendiaryCommand()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{1, 3}, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hill Giant");
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Damage-player mode cannot target a creature")
    void damagePlayerModeRejectsCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IncendiaryCommand()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 2, new int[]{0, 1}, List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("One remaining legal target allows its mode to resolve")
    void resolvesWithOneRemainingLegalTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new GhostQuarter());
        harness.setHand(player1, List.of(new IncendiaryCommand()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{0, 2},
                List.of(player2.getId(), land.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(land);

        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Incendiary Command");
    }

    @Test
    @DisplayName("Losing the only target prevents the untargeted mode from resolving")
    void doesNotWheelWhenOnlyTargetBecomesIllegal() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new GhostQuarter());
        harness.setHand(player1, List.of(new IncendiaryCommand(), new Mountain()));
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.setLibrary(player2, List.of(new Island()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castModalSorceryWithModes(player1, 0, 2, new int[]{2, 3}, List.of(land.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(land);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Mountain");
        harness.assertInHand(player2, "Forest");
        harness.assertNotInHand(player1, "Plains");
        harness.assertNotInHand(player2, "Island");
        harness.assertInGraveyard(player1, "Incendiary Command");
    }
}
