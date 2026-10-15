package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.t.TwistedImage;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StoicRebuttal.class, CopperMyr.class, Memnite.class, TwistedImage.class})
class StoicRebuttalTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell when cast for full cost")
    void countersCreatureSpellFullCost() {
        CopperMyr myr = new CopperMyr();
        harness.setHand(player1, List.of(myr));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new StoicRebuttal()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, myr.getId());

        harness.assertInGraveyard(player1, "Copper Myr");
        harness.assertNotOnBattlefield(player1, "Copper Myr");
    }

    @Test
    @DisplayName("Can be cast for {U}{U} with metalcraft (3 artifacts)")
    void castableWithMetalcraftReduction() {
        // Give player2 three artifacts for metalcraft
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new Memnite());

        CopperMyr myr = new CopperMyr();
        harness.setHand(player1, List.of(myr));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new StoicRebuttal()));
        // Only 2 blue mana is enough with metalcraft ({1} reduced), but not without it.
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, myr.getId());

        harness.assertInGraveyard(player1, "Copper Myr");
    }

    @Test
    @DisplayName("Cannot be cast for {U}{U} without metalcraft")
    void cannotCastWithReducedCostWithoutMetalcraft() {
        // Player2 has only 2 artifacts, not enough for metalcraft.
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new Memnite());

        CopperMyr myr = new CopperMyr();
        harness.setHand(player1, List.of(myr));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new StoicRebuttal()));
        // Only 2 blue mana is not enough without metalcraft.
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        // Should fail to cast because there is not enough mana without metalcraft.
        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
                harness.castInstant(player2, 0, myr.getId()));
    }

    @Test
    @DisplayName("Fizzles if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        CopperMyr myr = new CopperMyr();
        harness.setHand(player1, List.of(myr));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new StoicRebuttal()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, myr.getId());

        gd.stack.removeIf(se -> se.getCard().getName().equals("Copper Myr"));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player2, "Stoic Rebuttal");
    }

    @Test
    @DisplayName("Counters a noncreature spell")
    void countersNoncreatureSpell() {
        harness.addToBattlefield(player1, new Memnite());
        TwistedImage image = new TwistedImage();
        harness.setHand(player1, List.of(image));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new StoicRebuttal()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, findPermanent(player1, "Memnite").getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, image.getId());

        harness.assertInGraveyard(player1, "Twisted Image");
        harness.assertInGraveyard(player2, "Stoic Rebuttal");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent's artifacts do not enable metalcraft")
    void opponentsArtifactsDoNotReduceCost() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Memnite());
        }
        harness.addToBattlefield(player2, new Memnite());
        harness.addToBattlefield(player2, new Memnite());
        CopperMyr myr = new CopperMyr();
        harness.setHand(player1, List.of(myr));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new StoicRebuttal()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, myr.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player2, "Stoic Rebuttal");
    }

    @Test
    @DisplayName("Metalcraft never reduces the two blue mana requirement")
    void metalcraftDoesNotReduceColoredCost() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new Memnite());
        }
        CopperMyr myr = new CopperMyr();
        harness.setHand(player1, List.of(myr));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new StoicRebuttal()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, myr.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player2, "Stoic Rebuttal");
    }
}
