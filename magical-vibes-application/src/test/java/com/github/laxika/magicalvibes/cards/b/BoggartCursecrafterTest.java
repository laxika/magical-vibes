package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SkirkProspector;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BoggartCursecrafterTest extends BaseCardTest {

    @Test
    @DisplayName("Another Goblin dying deals 1 damage to each opponent")
    void goblinDeathDealsDamageToEachOpponent() {
        addBoggartCursecrafterReady(player1);
        harness.addToBattlefield(player1, new SkirkProspector());

        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        killCreature(player1, "Skirk Prospector");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    @DisplayName("A non-Goblin dying does not trigger Boggart Cursecrafter")
    void nonGoblinDeathDoesNotTrigger() {
        addBoggartCursecrafterReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        int opponentLifeBefore = gd.getLife(player2.getId());

        killCreature(player1, "Grizzly Bears");

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Boggart Cursecrafter does not trigger for its own death")
    void ownDeathDoesNotTrigger() {
        addBoggartCursecrafterReady(player1);

        int opponentLifeBefore = gd.getLife(player2.getId());

        killCreature(player1, "Boggart Cursecrafter");

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    private void killCreature(Player controller, String targetName) {
        harness.setHand(controller, List.of(new Shock()));
        harness.addMana(controller, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(controller, targetName);
        harness.castInstant(controller, 0, targetId);
        harness.passBothPriorities();
    }

    private void addBoggartCursecrafterReady(Player player) {
        Permanent permanent = new Permanent(new BoggartCursecrafter());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }
}
