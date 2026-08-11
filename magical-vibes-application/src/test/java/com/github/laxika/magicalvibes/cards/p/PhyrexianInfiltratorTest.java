package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
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
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(controls(player1.getId(), infiltrator.getId())).isTrue();
        assertThat(controls(player2.getId(), infiltrator.getId())).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }
}
