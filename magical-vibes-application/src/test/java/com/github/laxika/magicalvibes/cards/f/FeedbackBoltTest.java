package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeedbackBoltTest extends BaseCardTest {

    private void cast() {
        harness.setHand(player1, List.of(new FeedbackBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Deals damage equal to the number of artifacts you control")
    void dealsDamageEqualToArtifactCount() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());

        int before = gd.getLife(player2.getId());
        cast();

        assertThat(gd.getLife(player2.getId())).isEqualTo(before - 2);
    }

    @Test
    @DisplayName("Counts only artifacts controlled by the caster")
    void countsOnlyArtifactsControlledByCaster() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());

        int before = gd.getLife(player2.getId());
        cast();

        assertThat(gd.getLife(player2.getId())).isEqualTo(before - 1);
    }

    @Test
    @DisplayName("Deals no damage when you control no artifacts")
    void dealsNoDamageWithoutArtifacts() {
        int before = gd.getLife(player2.getId());
        cast();

        assertThat(gd.getLife(player2.getId())).isEqualTo(before);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FeedbackBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }
}
