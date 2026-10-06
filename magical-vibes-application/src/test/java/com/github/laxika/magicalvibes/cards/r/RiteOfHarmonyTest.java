package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BoonSatyr;
import com.github.laxika.magicalvibes.cards.f.FurnaceOfRath;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JoinTheDance;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.cards.m.MoonsilverKey;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiteOfHarmony.class, BoonSatyr.class, FurnaceOfRath.class, GrizzlyBears.class,
        JoinTheDance.class, MarchOfTheMachines.class, MoonsilverKey.class})
class RiteOfHarmonyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws when a creature you control enters")
    void drawsForControlledCreatureEntering() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castRiteFromHand();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws when an enchantment you control enters")
    void drawsForControlledEnchantmentEntering() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castRiteFromHand();

        harness.castFromHand(player1, new FurnaceOfRath(), "{1}{R}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An enchantment creature triggers only once")
    void enchantmentCreatureTriggersOnlyOnce() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        castRiteFromHand();

        harness.castFromHand(player1, new BoonSatyr(), "{1}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Flashback registers the same delayed trigger and exiles Rite of Harmony")
    void flashbackRegistersTriggerAndExilesSpell() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(new RiteOfHarmony()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFlashback(player1, 0, null);

        harness.assertNotInGraveyard(player1, "Rite of Harmony");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Rite of Harmony"));
    }

    @Test
    @DisplayName("Each simultaneously entering creature token draws a card")
    void drawsForEachCreatureToken() {
        harness.setLibrary(player1, List.of(new MoonsilverKey(), new MoonsilverKey(), new MoonsilverKey()));
        castRiteFromHand();
        castJoinTheDance();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Flashback creates a functioning draw trigger")
    void flashbackDrawsForEnteringTokens() {
        harness.setLibrary(player1, List.of(new MoonsilverKey(), new MoonsilverKey(), new MoonsilverKey()));
        harness.setGraveyard(player1, List.of(new RiteOfHarmony()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveFlashback(player1, 0, null);

        castJoinTheDance();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Multiple resolved Rites each draw for the same entering creatures")
    void multipleRitesStack() {
        harness.setLibrary(player1, List.of(new MoonsilverKey(), new MoonsilverKey(),
                new MoonsilverKey(), new MoonsilverKey(), new MoonsilverKey()));
        castRiteFromHand();
        castRiteFromHand();
        castJoinTheDance();
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Opposing creatures do not trigger the draw")
    void ignoresOpponentCreature() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castRiteFromHand();
        harness.castFromHand(player2, new BoonSatyr(), "{1}{G}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Boon Satyr");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ordinary artifacts do not trigger the draw")
    void ignoresNoncreatureArtifact() {
        harness.setLibrary(player1, List.of(new MoonsilverKey()));
        castRiteFromHand();
        harness.castFromHand(player1, new MoonsilverKey(), "{2}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Moonsilver Key");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An artifact entering as a creature draws a card")
    void drawsForArtifactAnimatedAsItEnters() {
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        harness.setLibrary(player1, List.of(new MoonsilverKey()));
        castRiteFromHand();
        harness.castFromHand(player1, new MoonsilverKey(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Moonsilver Key");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The draw trigger expires at the end of the turn")
    void expiresAtEndOfTurn() {
        castRiteFromHand();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new MoonsilverKey()));
        harness.castFromHand(player1, new BoonSatyr(), "{1}{G}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Boon Satyr");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The draw trigger remains active during the end step")
    void drawsDuringEndStep() {
        castRiteFromHand();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.setLibrary(player1, List.of(new MoonsilverKey()));
        harness.castFromHand(player1, new BoonSatyr(), "{1}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Creatures that entered before Rite resolved do not draw cards")
    void doesNotDrawRetroactively() {
        harness.setLibrary(player1, List.of(new MoonsilverKey()));
        castJoinTheDance();
        castRiteFromHand();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void castJoinTheDance() {
        harness.castFromHand(player1, new JoinTheDance(), "{G}{W}");
        harness.passBothPriorities();
    }

    private void castRiteFromHand() {
        harness.castFromHand(player1, new RiteOfHarmony(), "{G}{W}");
        harness.passBothPriorities();
    }
}
