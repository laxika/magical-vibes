package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AvariceTotemTest extends BaseCardTest {

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private boolean controls(UUID playerId, UUID permanentId) {
        return gd.playerBattlefields.get(playerId).stream()
                .anyMatch(permanent -> permanent.getId().equals(permanentId));
    }

    @Test
    @DisplayName("Exchanges control of itself and the target nonland permanent")
    void exchangesControl() {
        Permanent totem = harness.addToBattlefieldAndReturn(player1, new AvariceTotem());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(controls(player2.getId(), totem.getId())).isTrue();
        assertThat(controls(player1.getId(), target.getId())).isTrue();
    }

    @Test
    @DisplayName("Can target a nonland permanent controlled by the same player")
    void canTargetOwnNonlandPermanent() {
        Permanent totem = harness.addToBattlefieldAndReturn(player1, new AvariceTotem());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(controls(player1.getId(), totem.getId())).isTrue();
        assertThat(controls(player1.getId(), target.getId())).isTrue();
    }
}
