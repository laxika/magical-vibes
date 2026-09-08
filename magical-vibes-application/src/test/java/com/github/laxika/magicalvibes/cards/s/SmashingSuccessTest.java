package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmashingSuccessTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact and creates a Treasure")
    void destroysArtifactAndCreatesTreasure() {
        Permanent thopter = new Permanent(new Ornithopter());
        gd.playerBattlefields.get(player2.getId()).add(thopter);

        castSmashingSuccess(thopter.getId());

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Destroys target land without creating a Treasure")
    void destroysLandWithoutCreatingTreasure() {
        Permanent forest = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(forest);

        castSmashingSuccess(forest.getId());

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature that is neither an artifact nor a land")
    void cannotTargetCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);
        harness.setHand(player1, List.of(new SmashingSuccess()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void castSmashingSuccess(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new SmashingSuccess()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
