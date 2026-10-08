package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SokkasHaiku.class, Forest.class, GrizzlyBears.class})
class SokkasHaikuTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell, draws, mills three cards, and untaps a target land")
    void resolvesAllEffects() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        Forest drawn = new Forest();
        Forest milledOne = new Forest();
        Forest milledTwo = new Forest();
        Forest milledThree = new Forest();
        harness.setLibrary(player2, List.of(drawn, milledOne, milledTwo, milledThree));
        harness.setHand(player2, List.of(new SokkasHaiku()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.castInstant(player2, 0, bears.getId(), forest.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).contains(drawn);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(milledOne, milledTwo, milledThree);
    }

    @Test
    @DisplayName("Rejects a nonland permanent as the untap target")
    void rejectsNonlandUntapTarget() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new SokkasHaiku()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId(), creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @ParameterizedTest
    @CsvSource({"true, false", "false, true", "true, true"})
    @DisplayName("Resolves with one remaining target but does nothing when both targets leave")
    void handlesTargetsLeavingBeforeResolution(boolean spellLeaves, boolean landLeaves) {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();

        Forest drawn = new Forest();
        Forest milledOne = new Forest();
        Forest milledTwo = new Forest();
        Forest milledThree = new Forest();
        Forest remaining = new Forest();
        harness.setLibrary(player2, List.of(drawn, milledOne, milledTwo, milledThree, remaining));
        harness.setHand(player2, List.of(new SokkasHaiku()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.castInstant(player2, 0, bears.getId(), forest.getId());
        if (spellLeaves) {
            gd.stack.removeIf(entry -> entry.getTargetableId().equals(bears.getId()));
            gd.playerGraveyards.get(player1.getId()).add(bears);
        }
        if (landLeaves) {
            gd.playerBattlefields.get(player2.getId()).remove(forest);
            gd.playerGraveyards.get(player2.getId()).add(forest.getCard());
        }
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Sokka's Haiku");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        if (spellLeaves && landLeaves) {
            assertThat(gd.playerHands.get(player2.getId())).doesNotContain(drawn);
            assertThat(gd.playerDecks.get(player2.getId()))
                    .containsExactly(drawn, milledOne, milledTwo, milledThree, remaining);
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .doesNotContain(milledOne, milledTwo, milledThree);
        } else {
            assertThat(gd.playerHands.get(player2.getId())).contains(drawn);
            assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .contains(milledOne, milledTwo, milledThree);
            if (!landLeaves) {
                assertThat(forest.isTapped()).isFalse();
            }
        }
    }

    @Test
    @DisplayName("Draws first and mills only the remaining cards in a short library")
    void resolvesWithShortLibraryAndUntappedLand() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Forest drawn = new Forest();
        Forest milled = new Forest();
        harness.setLibrary(player2, List.of(drawn, milled));
        harness.setHand(player2, List.of(new SokkasHaiku()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.castInstant(player2, 0, bears.getId(), forest.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(milled).doesNotContain(drawn);
        assertThat(forest.isTapped()).isFalse();
    }
}
