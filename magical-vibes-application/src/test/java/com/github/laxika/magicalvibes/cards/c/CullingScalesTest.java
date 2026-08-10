package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CullingScalesTest extends BaseCardTest {

    @Test
    @DisplayName("Targets a lowest-mana-value nonland permanent and destroys it")
    void targetsLowestManaValueNonlandPermanent() {
        Permanent scales = harness.addToBattlefieldAndReturn(player1, new CullingScales());
        Permanent lowest = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent higher = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(lowest.getId())
                .doesNotContain(scales.getId(), higher.getId(), land.getId());

        harness.handlePermanentChosen(player1, lowest.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Allows choosing either permanent tied for lowest mana value")
    void allowsChoosingTiedLowestPermanent() {
        harness.addToBattlefield(player1, new CullingScales());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());

        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Can destroy itself when it has the lowest mana value")
    void canDestroyItself() {
        harness.addToBattlefield(player1, new CullingScales());
        Permanent scales = findPermanent(player1, "Culling Scales");

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(scales.getId());
        harness.handlePermanentChosen(player1, scales.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Culling Scales");
    }

    @Test
    @DisplayName("Does not destroy the target if it is no longer lowest on resolution")
    void doesNotDestroyTargetIfItIsNoLongerLowest() {
        harness.addToBattlefield(player1, new CullingScales());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }
}
