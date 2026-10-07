package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoblinChieftain;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowHarrier;
import com.github.laxika.magicalvibes.cards.m.MoongloveWinnower;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SqueakingPieSneak.class, GoblinChieftain.class, GoldmeadowHarrier.class,
        MoongloveWinnower.class, Ornithopter.class, Tarfire.class})
class SqueakingPieSneakTest extends BaseCardTest {

    @Test
    @DisplayName("Without another Goblin in hand it costs {1}{B} plus the additional {3}")
    void requiresExtraThreeWithoutGoblin() {
        // The Sneak itself is a Goblin but is on the stack, so it cannot satisfy its own reveal.
        harness.setHand(player1, List.of(new SqueakingPieSneak()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The additional {3} can be paid with mana when no Goblin is revealed")
    void payTheThreeWithMana() {
        SqueakingPieSneak sneak = new SqueakingPieSneak();
        harness.setHand(player1, List.of(sneak));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4); // {1} + {3}

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Squeaking Pie Sneak");
    }

    @Test
    @DisplayName("Revealing a Goblin card from hand lets it be cast for just {1}{B}")
    void revealGoblinAvoidsTheThree() {
        SqueakingPieSneak sneak = new SqueakingPieSneak();
        GoblinChieftain goblinInHand = new GoblinChieftain();
        harness.setHand(player1, List.of(sneak, goblinInHand));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Squeaking Pie Sneak");
        // Revealing does not remove the Goblin card from hand.
        harness.assertInHand(player1, "Goblin Chieftain");
    }

    @Test
    @DisplayName("Avoiding the additional mana publicly reveals the Goblin before resolution")
    void revealsGoblinAsCastingCost() {
        harness.setHand(player1, List.of(new SqueakingPieSneak(), new GoblinChieftain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("reveals")
                && entry.plainText().contains("Goblin Chieftain"));
        harness.assertInHand(player1, "Goblin Chieftain");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Squeaking Pie Sneak");
    }

    @Test
    @DisplayName("A noncreature Goblin card can satisfy the reveal cost")
    void canRevealKindredGoblin() {
        harness.setHand(player1, List.of(new SqueakingPieSneak(), new Tarfire()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Squeaking Pie Sneak");
        harness.assertInHand(player1, "Tarfire");
    }

    @Test
    void opponentGoblinCannotSatisfyReveal() {
        harness.setHand(player1, List.of(new SqueakingPieSneak()));
        harness.setHand(player2, List.of(new SqueakingPieSneak()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void battlefieldGoblinCannotSatisfyReveal() {
        harness.setHand(player1, List.of(new SqueakingPieSneak()));
        harness.addToBattlefield(player1, new SqueakingPieSneak());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void nonGoblinInHandCannotSatisfyReveal() {
        harness.setHand(player1, List.of(new SqueakingPieSneak(), new GoldmeadowHarrier()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void revealingGoblinDoesNotWaiveBlackMana() {
        harness.setHand(player1, List.of(new SqueakingPieSneak(), new Tarfire()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fearStopsNonblackNonartifactBlocker() {
        addCreatureReady(player1, new SqueakingPieSneak());
        addCreatureReady(player2, new GoldmeadowHarrier());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fearAllowsBlackBlocker() {
        addCreatureReady(player1, new SqueakingPieSneak());
        addCreatureReady(player2, new MoongloveWinnower());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    void fearAllowsColorlessArtifactBlocker() {
        addCreatureReady(player1, new SqueakingPieSneak());
        addCreatureReady(player2, new Ornithopter());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
