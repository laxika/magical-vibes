package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkeletonShardTest extends BaseCardTest {

    @Test
    @DisplayName("The {3} activation returns an artifact creature from the graveyard to hand")
    void genericActivationReturnsArtifactCreature() {
        Ornithopter ornithopter = new Ornithopter();
        addShardAndTarget(ornithopter);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, ornithopter.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("The {B} activation returns an artifact creature from the graveyard to hand")
    void blackActivationReturnsArtifactCreature() {
        Ornithopter ornithopter = new Ornithopter();
        addShardAndTarget(ornithopter);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, ornithopter.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("The activations cannot target a card that is not an artifact creature")
    void cannotTargetNonArtifactCreature() {
        harness.addToBattlefield(player1, new SkeletonShard());
        Card nonArtifact = new GrizzlyBears();
        Card nonCreature = new DarksteelRelic();
        harness.setGraveyard(player1, List.of(nonArtifact, nonCreature));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, nonArtifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addShardAndTarget(Card target) {
        harness.addToBattlefield(player1, new SkeletonShard());
        harness.setGraveyard(player1, List.of(target));
    }
}
