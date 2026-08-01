package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArmoryGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Has no vigilance while you control no Gate")
    void noVigilanceWithoutGate() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new ArmoryGuard());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, guard, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Has vigilance while you control a Gate")
    void vigilanceWithGate() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new ArmoryGuard());
        harness.addToBattlefield(player1, new RakdosGuildgate());

        assertThat(gqs.hasKeyword(gd, guard, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's Gate does not grant vigilance")
    void opponentGateDoesNotGrantVigilance() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new ArmoryGuard());
        harness.addToBattlefield(player2, new RakdosGuildgate());

        assertThat(gqs.hasKeyword(gd, guard, Keyword.VIGILANCE)).isFalse();
    }
}
