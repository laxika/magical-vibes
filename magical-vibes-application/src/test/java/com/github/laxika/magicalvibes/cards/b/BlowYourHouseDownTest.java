package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfFire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlowYourHouseDown.class, FountainOfYouth.class, GrizzlyBears.class, WallOfFire.class})
class BlowYourHouseDownTest extends BaseCardTest {

    @Test
    @DisplayName("Makes up to three target creatures unable to block and destroys targeted Walls")
    void makesCreaturesUnableToBlockAndDestroysWalls() {
        Permanent wall1 = harness.addToBattlefieldAndReturn(player2, new WallOfFire());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent wall2 = harness.addToBattlefieldAndReturn(player2, new WallOfFire());
        Permanent untargetedWall = harness.addToBattlefieldAndReturn(player2, new WallOfFire());
        prepareCast();

        harness.castSorcery(player1, 0, List.of(wall1.getId(), bear.getId(), wall2.getId()));
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(battlefield).extracting(Permanent::getId)
                .containsExactlyInAnyOrder(bear.getId(), untargetedWall.getId());
        assertThat(bear.isCantBlockThisTurn()).isTrue();
        assertThat(untargetedWall.isCantBlockThisTurn()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getId())
                .contains(wall1.getCard().getId(), wall2.getCard().getId());
    }

    @Test
    @DisplayName("Allows fewer than three targets")
    void allowsFewerThanThreeTargets() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();

        harness.castSorcery(player1, 0, List.of(bear.getId()));
        harness.passBothPriorities();

        assertThat(bear.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        prepareCast();

        UUID artifactId = artifact.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(artifactId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new BlowYourHouseDown()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
