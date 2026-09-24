package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CullingScales.class, Bonesplitter.class, AlphaMyr.class, Forest.class, Ornithopter.class})
class CullingScalesTest extends BaseCardTest {

    @Test
    @DisplayName("Targets a lowest-mana-value nonland permanent and destroys it")
    void targetsLowestManaValueNonlandPermanent() {
        Permanent scales = harness.addToBattlefieldAndReturn(player1, new CullingScales());
        Permanent lowest = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());
        Permanent higher = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(lowest.getId())
                .doesNotContain(scales.getId(), higher.getId(), land.getId());

        harness.handlePermanentChosen(player1, lowest.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Bonesplitter");
        harness.assertOnBattlefield(player2, "Alpha Myr");
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
    @DisplayName("Treats a face-down nonland permanent as having mana value zero")
    void treatsFaceDownNonlandPermanentAsManaValueZero() {
        harness.addToBattlefield(player1, new CullingScales());
        Permanent faceDown = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        faceDown.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent zeroManaValue = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(faceDown.getId(), zeroManaValue.getId());
    }

    @Test
    @DisplayName("Destroys the chosen permanent when another permanent ties for lowest before resolution")
    void destroysChosenPermanentWhenAnotherPermanentTiesForLowestBeforeResolution() {
        harness.addToBattlefield(player1, new CullingScales());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        Permanent tied = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(tied)
                .doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
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
    @DisplayName("Triggers only during its controller's upkeep")
    void triggersOnlyDuringItsControllersUpkeep() {
        harness.addToBattlefield(player1, new CullingScales());
        harness.addToBattlefield(player2, new Bonesplitter());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Bonesplitter");
    }

    @Test
    @DisplayName("Does not destroy the target if it is no longer lowest on resolution")
    void doesNotDestroyTargetIfItIsNoLongerLowest() {
        harness.addToBattlefield(player1, new CullingScales());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Bonesplitter");
    }
}
