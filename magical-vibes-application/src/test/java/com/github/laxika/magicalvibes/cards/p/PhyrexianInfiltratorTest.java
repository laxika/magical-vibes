package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianInfiltrator.class, RagingKavu.class, Island.class})
class PhyrexianInfiltratorTest extends BaseCardTest {

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private boolean controls(UUID playerId, UUID permanentId) {
        return gd.playerBattlefields.get(playerId).stream()
                .anyMatch(permanent -> permanent.getId().equals(permanentId));
    }

    @Test
    @DisplayName("Exchanges control of itself and the target creature")
    void exchangesControl() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new PhyrexianInfiltrator());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingKavu());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(controls(player2.getId(), infiltrator.getId())).isTrue();
        assertThat(controls(player1.getId(), target.getId())).isTrue();
    }

    @Test
    @DisplayName("Does nothing when the target creature has the same controller")
    void doesNothingForCreatureWithSameController() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new PhyrexianInfiltrator());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RagingKavu());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(controls(player1.getId(), infiltrator.getId())).isTrue();
        assertThat(controls(player1.getId(), target.getId())).isTrue();
    }

    @Test
    @DisplayName("Does not exchange control when the target leaves before resolution")
    void doesNothingWhenTargetLeavesBeforeResolution() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new PhyrexianInfiltrator());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingKavu());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(controls(player1.getId(), infiltrator.getId())).isTrue();
        assertThat(controls(player2.getId(), infiltrator.getId())).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Exchanges back after a second activation changes its controller")
    void exchangesBackAfterSecondActivationChangesItsController() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new PhyrexianInfiltrator());
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new RagingKavu());
        Permanent opponentsTarget = harness.addToBattlefieldAndReturn(player2, new RagingKavu());
        addActivationMana();
        addActivationMana();

        harness.activateAbility(player1, 0, null, ownTarget.getId());
        harness.activateAbility(player1, 0, null, opponentsTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(controls(player1.getId(), infiltrator.getId())).isTrue();
        assertThat(controls(player1.getId(), opponentsTarget.getId())).isTrue();
        assertThat(controls(player2.getId(), ownTarget.getId())).isTrue();
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNoncreature() {
        Permanent infiltrator = harness.addToBattlefieldAndReturn(player1, new PhyrexianInfiltrator());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(controls(player1.getId(), infiltrator.getId())).isTrue();
    }
}
