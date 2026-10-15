package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RedSunsZenith.class, GrizzlyBears.class, SerraAngel.class, Shock.class})
class RedSunsZenithTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to target player")
    void dealsXDamageToPlayer() {
        harness.setHand(player1, List.of(new RedSunsZenith()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 5, player2.getId());

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("X=0 deals no damage")
    void xZeroDealsNoDamage() {
        harness.setHand(player1, List.of(new RedSunsZenith()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 0, player2.getId());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deals lethal X damage to target creature, exiling it")
    void dealsLethalXDamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RedSunsZenith()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 2, targetId);

        // Lethal damage removes the creature via the exile replacement.
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not destroy creature with toughness greater than X")
    void doesNotDestroyCreatureWithHigherToughness() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new RedSunsZenith()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Serra Angel");
        harness.castAndResolveSorcery(player1, 0, 3, targetId);

        // Serra Angel (4/4) should survive 3 damage
        harness.assertOnBattlefield(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Creature killed by Red Sun's Zenith is exiled instead of going to graveyard")
    void creatureKilledIsExiledInsteadOfDying() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RedSunsZenith()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 2, targetId);

        // Grizzly Bears should be exiled, NOT in graveyard
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Creature that survives damage is not exiled")
    void creatureThatSurvivesIsNotExiled() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new RedSunsZenith()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Serra Angel");
        harness.castAndResolveSorcery(player1, 0, 1, targetId);

        // Serra Angel should still be on the battlefield
        harness.assertOnBattlefield(player2, "Serra Angel");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Serra Angel"));
    }

    @Test
    @DisplayName("Red Sun's Zenith is shuffled into owner's library instead of going to graveyard")
    void shuffledIntoLibraryAfterResolution() {
        harness.setHand(player1, List.of(new RedSunsZenith()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        // Should NOT be in graveyard
        harness.assertNotInGraveyard(player1, "Red Sun's Zenith");
        // Should be shuffled into library
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Red Sun's Zenith"));
    }

    @Test
    @DisplayName("Casting puts it on the stack as sorcery")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new RedSunsZenith()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Red Sun's Zenith");
        assertThat(entry.getXValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setHand(player1, List.of(new RedSunsZenith()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature damaged by Zenith is exiled when killed by another spell that turn")
    void laterLethalDamageExilesCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RedSunsZenith(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castAndResolveSorcery(player1, 0, 1, targetId);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Zero damage does not cause a creature killed later to be exiled")
    void zeroDamageDoesNotApplyExileReplacement() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RedSunsZenith(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castAndResolveSorcery(player1, 0, 0, targetId);
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Red Sun's Zenith"));
    }

    @Test
    @DisplayName("Zenith goes to the graveyard without shuffling when its only target becomes illegal")
    void illegalTargetPreventsShuffle() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        RedSunsZenith zenith = new RedSunsZenith();
        harness.setHand(player1, List.of(zenith, new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castSorcery(player1, 0, 2, targetId);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Red Sun's Zenith");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(zenith);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Zenith's exile replacement expires at the end of the turn")
    void exileReplacementExpiresAtEndOfTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RedSunsZenith()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castAndResolveSorcery(player1, 0, 1, targetId);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, targetId);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
