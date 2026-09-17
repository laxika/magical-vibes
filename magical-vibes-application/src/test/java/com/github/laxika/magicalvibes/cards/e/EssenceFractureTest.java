package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EssenceFracture.class, ElvishWarrior.class, GlorySeeker.class, Island.class})
class EssenceFractureTest extends BaseCardTest {

    @Test
    @DisplayName("Returns two target creatures to their owners' hands")
    void returnsTwoTargetCreatures() {
        Permanent glorySeeker = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        Permanent elvishWarrior = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());

        prepareEssenceFracture();
        harness.castAndResolveSorcery(player1, 0, List.of(glorySeeker.getId(), elvishWarrior.getId()));

        harness.assertNotOnBattlefield(player1, "Glory Seeker");
        harness.assertNotOnBattlefield(player2, "Elvish Warrior");
        harness.assertInHand(player1, "Glory Seeker");
        harness.assertInHand(player2, "Elvish Warrior");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());

        prepareEssenceFracture();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId(), island.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Requires exactly two target creatures")
    void requiresExactlyTwoTargetCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());

        prepareEssenceFracture();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose 2 targets");
    }

    @Test
    @DisplayName("Cannot choose the same creature for both targets")
    void cannotChooseSameCreatureTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());

        prepareEssenceFracture();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    @Test
    @DisplayName("Cycling discards Essence Fracture and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new EssenceFracture()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Essence Fracture");
        harness.assertInHand(player1, "Glory Seeker");
    }

    private void prepareEssenceFracture() {
        harness.setHand(player1, List.of(new EssenceFracture()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
