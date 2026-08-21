package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PriestOfForgottenGodsTest extends BaseCardTest {

    @Test
    @DisplayName("Target player loses life, sacrifices a creature, and you get mana and a card")
    void targetPlayerLosesLifeSacrificesCreatureAndControllerGetsManaAndCard() {
        addCreatureReady(player1, new PriestOfForgottenGods());
        Permanent firstCostCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent targetCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        int targetLifeBefore = gd.getLife(player2.getId());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, firstCostCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(targetLifeBefore - 2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(targetCreature.getId()));
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Each target player loses life, including a target with no creature")
    void eachTargetPlayerLosesLife() {
        addCreatureReady(player1, new PriestOfForgottenGods());
        Permanent firstCostCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent targetCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());
        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player1.getId(), player2.getId()));
        harness.handlePermanentChosen(player1, firstCostCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore - 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(targetCreature.getId()));
    }

    @Test
    @DisplayName("The ability can resolve with no targets and still gives mana and a card")
    void resolvesWithNoTargets() {
        addCreatureReady(player1, new PriestOfForgottenGods());
        Permanent firstCostCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        int opponentLifeBefore = gd.getLife(player2.getId());
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.handlePermanentChosen(player1, firstCostCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
