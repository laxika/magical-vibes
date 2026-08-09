package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoggSalvageTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an artifact when cast for free with an opponent's Island and your Mountain")
    void destroysArtifactWithAlternateCost() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Island());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new MoggSalvage()));

        harness.castWithAlternateCost(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Mogg Salvage");
    }

    @Test
    @DisplayName("Alternate cast requires an opponent's Island and your Mountain")
    void alternateCastRequiresBothLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new MoggSalvage()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifact() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Island());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new MoggSalvage()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
