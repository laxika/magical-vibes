package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        harness.passPriority(player1);
        harness.getGameService().playCard(gd, player2, 0, 0, bears.getId(), null, List.of(forest.getId()), List.of());
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
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.getGameService().playCard(
                gd, player2, 0, 0, bears.getId(), null, List.of(creature.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }
}
