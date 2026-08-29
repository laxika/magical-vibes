package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrzasRebuffTest extends BaseCardTest {

    @Test
    @DisplayName("Counter mode counters the target spell")
    void counterModeCountersSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new UrzasRebuff()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{0}, bears.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Tap mode taps up to two target creatures")
    void tapModeTapsUpToTwoCreatures() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        Permanent third = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UrzasRebuff()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castModalInstant(player1, 0, 1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tap mode may choose no creatures")
    void tapModeMayChooseNoCreatures() {
        harness.setHand(player1, List.of(new UrzasRebuff()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Tap mode rejects a noncreature target")
    void tapModeRejectsNoncreatureTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setHand(player1, List.of(new UrzasRebuff()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
