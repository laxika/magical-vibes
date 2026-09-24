package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(VulshokBerserker.class)
class VulshokBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack the turn it enters the battlefield")
    void canAttackTheTurnItEnters() {
        Permanent berserker = harness.addToBattlefieldAndReturn(player1, new VulshokBerserker());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(berserker.isAttacking()).isTrue();
    }
}
