package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DampingEngineTest extends BaseCardTest {

    @Test
    @DisplayName("Player with more permanents than every other player cannot cast a creature spell")
    void leaderCannotCastCreatureSpell() {
        harness.addToBattlefield(player1, new DampingEngine());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Player with more permanents than every other player cannot play a land")
    void leaderCannotPlayLand() {
        harness.addToBattlefield(player1, new DampingEngine());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The restriction does not apply when permanent counts are tied")
    void equalPermanentCountsAreAllowed() {
        harness.addToBattlefield(player1, new DampingEngine());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).contains(0);
    }

    @Test
    @DisplayName("The controller may sacrifice a permanent to ignore Damping Engine until end of turn")
    void sacrificePermanentIgnoresRestriction() {
        harness.addToBattlefield(player1, new DampingEngine());
        Permanent sacrificeTarget = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player1.getId())).isEmpty();

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sacrificeTarget.getId());
        harness.passBothPriorities();

        assertThat(availability.getPlayableCardIndices(gd, player1.getId())).contains(0);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof DampingEngine)
                .anyMatch(p -> p.getCard() instanceof GrizzlyBears);
    }
}
