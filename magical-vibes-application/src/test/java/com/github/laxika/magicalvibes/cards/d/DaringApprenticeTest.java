package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaringApprentice.class, Fog.class, GrizzlyBears.class})
class DaringApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Counters target spell, sacrificing itself as a cost")
    void countersTargetSpell() {
        Permanent apprentice = harness.addToBattlefieldAndReturn(player1, new DaringApprentice());
        apprentice.setSummoningSick(false);

        GrizzlyBears bears = new GrizzlyBears();

        // Player2 casts Grizzly Bears
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, bears, "{1}{G}");
        harness.passPriority(player2);

        // Player1 activates Daring Apprentice targeting Grizzly Bears
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Grizzly Bears is countered (into player2's graveyard, not on battlefield)
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        // Daring Apprentice sacrificed as a cost
        harness.assertInGraveyard(player1, "Daring Apprentice");
        harness.assertNotOnBattlefield(player1, "Daring Apprentice");

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick (has a tap cost)")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new DaringApprentice());

        GrizzlyBears bears = new GrizzlyBears();

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, bears, "{1}{G}");
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

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, bears, "{1}{G}");
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, bears.getId());

        // Remove the target spell before the ability resolves
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();

        // Daring Apprentice is still sacrificed (cost was already paid)
        harness.assertInGraveyard(player1, "Daring Apprentice");
    }

    @Test
    @DisplayName("Counters a noncreature spell")
    void countersNoncreatureSpell() {
        Permanent apprentice = harness.addToBattlefieldAndReturn(player1, new DaringApprentice());
        apprentice.setSummoningSick(false);

        Fog fog = new Fog();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, fog, "{G}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, fog.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fog");
        harness.assertNotOnBattlefield(player2, "Fog");
        harness.assertInGraveyard(player1, "Daring Apprentice");
        assertThat(gd.stack).isEmpty();
    }
}
