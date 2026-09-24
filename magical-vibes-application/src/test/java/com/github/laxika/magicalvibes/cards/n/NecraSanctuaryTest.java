package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CoalitionHonorGuard;
import com.github.laxika.magicalvibes.cards.c.Cromat;
import com.github.laxika.magicalvibes.cards.g.GaeasSkyfolk;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NecraSanctuary.class, CoalitionHonorGuard.class, Cromat.class, GaeasSkyfolk.class})
class NecraSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Target player loses 1 life when you control a green permanent only")
    void losesOneLifeWithGreenPermanentOnly() {
        harness.addToBattlefield(player1, new NecraSanctuary());
        harness.addToBattlefield(player1, new GaeasSkyfolk());

        resolveUpkeepTargetingPlayer2();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Target player loses 1 life when you control a white permanent only")
    void losesOneLifeWithWhitePermanentOnly() {
        harness.addToBattlefield(player1, new NecraSanctuary());
        harness.addToBattlefield(player1, new CoalitionHonorGuard());

        resolveUpkeepTargetingPlayer2();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Target player loses 3 life when you control green and white permanents")
    void losesThreeLifeWithGreenAndWhitePermanents() {
        harness.addToBattlefield(player1, new NecraSanctuary());
        harness.addToBattlefield(player1, new GaeasSkyfolk());
        harness.addToBattlefield(player1, new CoalitionHonorGuard());

        resolveUpkeepTargetingPlayer2();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Target player loses 3 life when one permanent is both green and white")
    void losesThreeLifeWithOneGreenAndWhitePermanent() {
        harness.addToBattlefield(player1, new NecraSanctuary());
        harness.addToBattlefield(player1, new Cromat());

        resolveUpkeepTargetingPlayer2();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Uses 3 life loss if a white permanent enters before resolution")
    void usesThreeLifeLossIfWhitePermanentEntersBeforeResolution() {
        harness.addToBattlefield(player1, new NecraSanctuary());
        harness.addToBattlefield(player1, new GaeasSkyfolk());

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new CoalitionHonorGuard());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Falls back to 1 life loss if a white permanent leaves before resolution")
    void fallsBackToOneLifeLossIfWhitePermanentLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new NecraSanctuary());
        harness.addToBattlefield(player1, new GaeasSkyfolk());
        Permanent whitePermanent = harness.addToBattlefieldAndReturn(player1, new CoalitionHonorGuard());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(whitePermanent);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger without a green or white permanent")
    void doesNotTriggerWithoutGreenOrWhitePermanent() {
        harness.addToBattlefield(player1, new NecraSanctuary());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger for green or white permanents controlled by an opponent")
    void doesNotTriggerForOpponentsColoredPermanents() {
        harness.addToBattlefield(player1, new NecraSanctuary());
        harness.addToBattlefield(player2, new GaeasSkyfolk());
        harness.addToBattlefield(player2, new CoalitionHonorGuard());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Triggers only during the controller's upkeep")
    void triggersOnlyDuringControllersUpkeep() {
        harness.addToBattlefield(player1, new NecraSanctuary());
        harness.addToBattlefield(player1, new GaeasSkyfolk());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does nothing if the qualifying permanent leaves before resolution")
    void doesNothingIfQualifyingPermanentLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new NecraSanctuary());
        Permanent greenPermanent = harness.addToBattlefieldAndReturn(player1, new GaeasSkyfolk());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(greenPermanent);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Allows targeting either player")
    void allowsTargetingEitherPlayer() {
        harness.addToBattlefield(player1, new NecraSanctuary());
        harness.addToBattlefield(player1, new GaeasSkyfolk());

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(player1.getId(), player2.getId());
    }

    private void resolveUpkeepTargetingPlayer2() {
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }
}
