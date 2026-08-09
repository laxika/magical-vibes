package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HostileMinotaurTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack the turn it enters the battlefield")
    void canAttackTheTurnItEnters() {
        Permanent minotaur = harness.addToBattlefieldAndReturn(player1, new HostileMinotaur());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(minotaur.isAttacking()).isTrue();
    }
}
