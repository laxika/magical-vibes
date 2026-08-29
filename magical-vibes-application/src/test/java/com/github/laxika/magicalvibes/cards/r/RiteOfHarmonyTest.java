package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BoonSatyr;
import com.github.laxika.magicalvibes.cards.f.FurnaceOfRath;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiteOfHarmony.class, BoonSatyr.class, FurnaceOfRath.class, GrizzlyBears.class})
class RiteOfHarmonyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws when a creature you control enters")
    void drawsForControlledCreatureEntering() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castRiteFromHand();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws when an enchantment you control enters")
    void drawsForControlledEnchantmentEntering() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castRiteFromHand();

        harness.setHand(player1, List.of(new FurnaceOfRath()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An enchantment creature triggers only once")
    void enchantmentCreatureTriggersOnlyOnce() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        castRiteFromHand();

        harness.setHand(player1, List.of(new BoonSatyr()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
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

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Rite of Harmony");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Rite of Harmony"));
    }

    private void castRiteFromHand() {
        harness.setHand(player1, List.of(new RiteOfHarmony()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
