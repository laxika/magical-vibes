package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WurmsTooth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BastionInventorTest extends BaseCardTest {

    @Test
    @DisplayName("Improvise taps an artifact to pay generic mana")
    void improviseTapsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WurmsTooth());
        harness.setHand(player1, List.of(new BastionInventor()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).contains(0);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId()));

        assertThat(artifact.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Bastion Inventor")).isNotNull();
    }

    @Test
    @DisplayName("Improvise cannot tap a nonartifact permanent")
    void improviseRejectsNonartifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BastionInventor()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> gs.playCard(
                gd, player1, 0, 0, null, null, List.of(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("is not an artifact");
        assertThat(creature.isTapped()).isFalse();
    }
}
