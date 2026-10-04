package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.cards.s.ScrollOfAvacyn;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({GoldnightRedeemer.class, MoorlandInquisitor.class, Cloudshift.class, ScrollOfAvacyn.class})
class GoldnightRedeemerTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life for each other creature you control")
    void gainsTwoLifePerOtherOwnCreature() {
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new GoldnightRedeemer()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.setLife(player1, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Does not count itself")
    void doesNotCountItself() {
        harness.setHand(player1, List.of(new GoldnightRedeemer()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.setLife(player1, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not count creatures the opponent controls")
    void ignoresOpponentCreatures() {
        harness.addToBattlefield(player2, new MoorlandInquisitor());
        harness.addToBattlefield(player2, new MoorlandInquisitor());
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new GoldnightRedeemer()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.setLife(player1, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not count noncreature permanents")
    void ignoresNoncreaturePermanents() {
        harness.addToBattlefield(player1, new ScrollOfAvacyn());
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new GoldnightRedeemer()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.setLife(player1, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Original trigger counts a blinked Redeemer as another creature")
    void countsReturnedRedeemerForOriginalTrigger() {
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new GoldnightRedeemer(), new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.setLife(player1, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Goldnight Redeemer"));
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.passBothPriorities();
        harness.assertLife(player1, 26);
    }
}
