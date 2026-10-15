package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GoblinArsonist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VolleyVeteran.class, GoblinArsonist.class, GrizzlyBears.class, Shock.class})
class VolleyVeteranTest extends BaseCardTest {

    @Test
    void dealsOneDamageForItselfAsGoblin() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castVolleyVeteran(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void dealsDamageForEachGoblinControlled() {
        harness.addToBattlefield(player1, new GoblinArsonist());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castVolleyVeteran(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void countsOnlyGoblinsControlledByVolleyVeteransController() {
        harness.addToBattlefield(player2, new GoblinArsonist());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castVolleyVeteran(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID ownBearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new VolleyVeteran()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownBearId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countsGoblinsAddedBeforeTriggerResolves() {
        harness.addToBattlefield(player2, new VolleyVeteran());
        castVolleyVeteran(player2, "Volley Veteran");
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new VolleyVeteran());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Volley Veteran");
        harness.assertNotOnBattlefield(player2, "Volley Veteran");
    }

    @Test
    void dealsZeroDamageWhenItsOnlyGoblinLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new VolleyVeteran());
        UUID targetId = harness.getPermanentId(player2, "Volley Veteran");
        castVolleyVeteran(player2, "Volley Veteran");
        harness.passBothPriorities();

        UUID sourceId = harness.getPermanentId(player1, "Volley Veteran");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, sourceId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Volley Veteran");
        harness.assertOnBattlefield(player2, "Volley Veteran");
        assertThat(gqs.findPermanentById(gd, targetId).getMarkedDamage()).isZero();
    }

    @Test
    void sourceLeavingDoesNotStopDamageFromRemainingGoblins() {
        harness.addToBattlefield(player2, new VolleyVeteran());
        UUID targetId = harness.getPermanentId(player2, "Volley Veteran");
        castVolleyVeteran(player2, "Volley Veteran");
        harness.passBothPriorities();
        UUID sourceId = harness.getPermanentId(player1, "Volley Veteran");
        harness.addToBattlefield(player1, new VolleyVeteran());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, sourceId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Volley Veteran");
        assertThat(gqs.findPermanentById(gd, targetId).getMarkedDamage()).isEqualTo(1);
    }

    private void castVolleyVeteran(com.github.laxika.magicalvibes.model.Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(targetOwner, targetName);
        harness.setHand(player1, List.of(new VolleyVeteran()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
