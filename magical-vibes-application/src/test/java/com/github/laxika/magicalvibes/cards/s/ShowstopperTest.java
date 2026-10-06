package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Showstopper.class, DoomBlade.class, GrizzlyBears.class})
class ShowstopperTest extends BaseCardTest {

    private void castShowstopper() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new Showstopper(), "{1}{B}{R}");
        harness.passBothPriorities();
    }

    private void killWithDoomBlade(UUID permanentId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, permanentId);
    }

    @Test
    @DisplayName("A creature that dies deals 2 damage to the chosen opponent creature")
    void deathTriggerDealsTwoDamage() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID myBear = harness.getPermanentId(player1, "Grizzly Bears");
        UUID theirBear = harness.getPermanentId(player2, "Grizzly Bears");

        castShowstopper();
        killWithDoomBlade(myBear);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handlePermanentChosen(player1, theirBear);
        harness.passBothPriorities();

        // 2 damage kills the 2/2 Grizzly Bears.
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The granted trigger can only target creatures an opponent controls")
    void onlyOpponentCreaturesAreLegalTargets() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent dying = gd.playerBattlefields.get(player1.getId()).getFirst();
        UUID survivor = gd.playerBattlefields.get(player1.getId()).get(1).getId();
        UUID theirBear = harness.getPermanentId(player2, "Grizzly Bears");

        castShowstopper();
        killWithDoomBlade(dying.getId());

        var choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(theirBear);
        assertThat(choice.validIds()).doesNotContain(survivor);
    }

    @Test
    @DisplayName("A creature that enters after Showstopper resolves does not gain the ability")
    void laterCreatureDoesNotGainTheAbility() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castShowstopper();

        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent late = gd.playerBattlefields.get(player1.getId()).get(1);

        killWithDoomBlade(late.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The granted death trigger wears off at end of turn")
    void grantWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addToBattlefield(player2, new GrizzlyBears());

        castShowstopper();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        killWithDoomBlade(bear.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponent creatures do not gain the death trigger")
    void opponentCreatureDoesNotGainAbility() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        castShowstopper();
        killWithDoomBlade(opponentCreature);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The granted ability still triggers during the end step")
    void abilityLastsThroughEndStep() {
        UUID dyingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        castShowstopper();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, dyingCreature);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(target);
        harness.handlePermanentChosen(player1, target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A death with no opposing creature leaves no trigger on the stack")
    void noLegalTarget() {
        UUID dyingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        castShowstopper();
        killWithDoomBlade(dyingCreature);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
