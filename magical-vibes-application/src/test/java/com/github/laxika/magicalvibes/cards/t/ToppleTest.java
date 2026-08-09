package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ToppleTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the creature with the greatest power on the battlefield")
    void exilesGreatestPowerCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castTopple(target);

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("Allows choosing any creature tied for greatest power")
    void allowsTiedGreatestPowerCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        castTopple(first);

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("Rejects targeting a creature that is not tied for greatest power")
    void rejectsLowerPowerTarget() {
        Permanent lower = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Topple()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, lower.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castTopple(Permanent target) {
        harness.setHand(player1, List.of(new Topple()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
