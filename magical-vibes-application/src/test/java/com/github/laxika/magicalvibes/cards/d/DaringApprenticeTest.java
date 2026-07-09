package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DaringApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Counters target spell, sacrificing itself as a cost")
    void countersTargetSpell() {
        Permanent apprentice = harness.addToBattlefieldAndReturn(player1, new DaringApprentice());
        apprentice.setSummoningSick(false);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);

        // Player2 casts Grizzly Bears
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        // Player1 activates Daring Apprentice targeting Grizzly Bears
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Grizzly Bears is countered (into player2's graveyard, not on battlefield)
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Daring Apprentice sacrificed as a cost
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Daring Apprentice"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Daring Apprentice"));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick (has a tap cost)")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new DaringApprentice());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target spell leaves the stack, but the sacrifice still happens")
    void fizzlesIfTargetRemoved() {
        Permanent apprentice = harness.addToBattlefieldAndReturn(player1, new DaringApprentice());
        apprentice.setSummoningSick(false);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, bears.getId());

        // Remove the target spell before the ability resolves
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();

        // Daring Apprentice is still sacrificed (cost was already paid)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Daring Apprentice"));
    }
}
