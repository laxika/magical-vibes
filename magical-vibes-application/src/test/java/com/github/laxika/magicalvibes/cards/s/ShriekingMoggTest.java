package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TerrainGenerator;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShriekingMogg.class, SpinelessThug.class, TerrainGenerator.class})
class ShriekingMoggTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps all other creatures on both battlefields")
    void etbTapsAllOtherCreatures() {
        harness.addToBattlefield(player1, new SpinelessThug());
        harness.addToBattlefield(player2, new SpinelessThug());
        harness.castFromHand(player1, new ShriekingMogg(), "{1}{R}");

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Spineless Thug").isTapped()).isTrue();
        assertThat(findPermanent(player2, "Spineless Thug").isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB does not tap Shrieking Mogg itself")
    void etbDoesNotTapSelf() {
        harness.castFromHand(player1, new ShriekingMogg(), "{1}{R}");

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Shrieking Mogg").isTapped()).isFalse();
    }

    @Test
    @DisplayName("ETB taps another Shrieking Mogg but not the entering one")
    void etbTapsAnotherCopy() {
        Permanent existingMogg = harness.addToBattlefieldAndReturn(player1, new ShriekingMogg());
        harness.castFromHand(player1, new ShriekingMogg(), "{1}{R}");

        resolveAllTriggers();

        assertThat(existingMogg.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Shrieking Mogg")).hasSize(2);
        assertThat(findPermanents(player1, "Shrieking Mogg").get(1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("ETB does not tap noncreature permanents")
    void etbDoesNotTapNoncreatures() {
        harness.addToBattlefield(player2, new TerrainGenerator());
        harness.castFromHand(player1, new ShriekingMogg(), "{1}{R}");

        resolveAllTriggers();

        assertThat(findPermanent(player2, "Terrain Generator").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Haste allows Shrieking Mogg to attack the turn it enters")
    void hasteAllowsAttackingImmediately() {
        harness.castFromHand(player1, new ShriekingMogg(), "{1}{R}");
        resolveAllTriggers();

        declareAttackers(List.of(0));

        assertThat(findPermanent(player1, "Shrieking Mogg").isAttackedThisTurn()).isTrue();
    }
}
