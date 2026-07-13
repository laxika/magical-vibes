package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StrongholdAssassinTest extends BaseCardTest {

    private Permanent setup() {
        Permanent assassin = harness.addToBattlefieldAndReturn(player1, new StrongholdAssassin());
        assassin.setSummoningSick(false);
        return assassin;
    }

    private int idxOf(Permanent p) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(p);
    }

    @Test
    @DisplayName("Taps, sacrifices a creature, and destroys the target nonblack creature")
    void destroysNonblackTarget() {
        Permanent assassin = setup();
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID fodderId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, idxOf(assassin), 0, null, targetId);
        harness.handlePermanentChosen(player1, fodderId);
        harness.passBothPriorities();

        // Fodder sacrificed as cost, assassin tapped
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(assassin.isTapped()).isTrue();

        // Target destroyed
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent assassin = setup();
        harness.addToBattlefield(player2, new BogImp());
        UUID bogImpId = harness.getPermanentId(player2, "Bog Imp");

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(assassin), 0, null, bogImpId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a player — the ability only targets creatures")
    void cannotTargetPlayer() {
        Permanent assassin = setup();

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(assassin), 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
