package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.w.WallOfFire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClearAPath.class, WallOfFire.class, RuneclawBear.class, TurnToFrog.class})
class ClearAPathTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature with defender")
    void destroysCreatureWithDefender() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfFire());

        prepare();
        harness.castAndResolveSorcery(player1, 0, wall.getId());

        harness.assertNotOnBattlefield(player2, "Wall of Fire");
        harness.assertInGraveyard(player2, "Wall of Fire");
    }

    @Test
    @DisplayName("Cannot target a creature without defender")
    void cannotTargetCreatureWithoutDefender() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        prepare();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A regeneration shield saves the creature")
    void regenerationShieldSavesTheCreature() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfFire());
        wall.setRegenerationShield(1);

        prepare();
        harness.castAndResolveSorcery(player1, 0, wall.getId());

        harness.assertOnBattlefield(player2, "Wall of Fire");
        assertThat(wall.isTapped()).isTrue();
        assertThat(wall.getRegenerationShield()).isZero();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can destroy its controller's creature with defender")
    void destroysOwnCreatureWithDefender() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfFire());

        prepare();
        harness.castAndResolveSorcery(player1, 0, wall.getId());

        harness.assertNotOnBattlefield(player1, "Wall of Fire");
        harness.assertInGraveyard(player1, "Wall of Fire");
        harness.assertInGraveyard(player1, "Clear a Path");
    }

    @Test
    @DisplayName("Does not destroy a target that loses defender before resolution")
    void targetLosingDefenderBecomesIllegal() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfFire());
        prepare();
        harness.castSorcery(player1, 0, wall.getId());

        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, wall.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wall of Fire");
        harness.assertInGraveyard(player1, "Clear a Path");
        harness.assertInGraveyard(player2, "Turn to Frog");
        assertThat(gd.stack).isEmpty();
    }

    private void prepare() {
        harness.setHand(player1, List.of(new ClearAPath()));
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
