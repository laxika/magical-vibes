package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZombieTrailblazer.class, Forest.class, GrizzlyBears.class})
class ZombieTrailblazerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping a Zombie makes the target land a Swamp until end of turn")
    void makesTargetLandSwampUntilEndOfTurn() {
        Permanent trailblazer = addCreatureReady(player1, new ZombieTrailblazer());
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, forestId);
        harness.passBothPriorities();

        Permanent forest = gqs.findPermanentById(gd, forestId);
        assertThat(trailblazer.isTapped()).isTrue();
        assertThat(forest.getTransientLandTypeOverride()).isEqualTo(CardSubtype.SWAMP);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(forest.getTransientLandTypeOverride()).isNull();
    }

    @Test
    @DisplayName("Tapping a Zombie gives the target creature swampwalk until end of turn")
    void grantsSwampwalkUntilEndOfTurn() {
        Permanent trailblazer = addCreatureReady(player1, new ZombieTrailblazer());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(trailblazer.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.SWAMPWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SWAMPWALK)).isFalse();
    }

    @Test
    @DisplayName("A non-Zombie cannot be tapped to pay the ability")
    void requiresAnUntappedZombieToPay() {
        Permanent trailblazer = addCreatureReady(player1, new ZombieTrailblazer());
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, forestId);
        harness.passBothPriorities();

        addCreatureReady(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThat(trailblazer.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }

    @Test
    @DisplayName("The land ability cannot target a creature")
    void landAbilityRequiresLandTarget() {
        Permanent trailblazer = addCreatureReady(player1, new ZombieTrailblazer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, trailblazer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }
}
