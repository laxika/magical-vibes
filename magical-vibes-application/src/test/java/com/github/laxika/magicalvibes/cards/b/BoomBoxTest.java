package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoomBox.class, Forest.class, GrizzlyBears.class, MindStone.class})
class BoomBoxTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys one artifact, one creature, and one land")
    void destroysOneOfEachType() {
        addBoomBox();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        addMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(artifact.getId(), creature.getId(), land.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Boom Box");
        harness.assertInGraveyard(player2, "Mind Stone");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("May choose only a creature or only a land")
    void independentlyOptionalTargets() {
        addBoomBox();
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        addMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(land.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Boom Box");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("May choose no targets")
    void mayChooseNoTargets() {
        addBoomBox();
        addMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Boom Box");
    }

    @Test
    @DisplayName("Rejects two artifact targets")
    void rejectsTwoArtifacts() {
        addBoomBox();
        Permanent first = harness.addToBattlefieldAndReturn(player2, new MindStone());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new MindStone());
        addMana();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addBoomBox() {
        harness.addToBattlefieldAndReturn(player1, new BoomBox());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}
