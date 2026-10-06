package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
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

@CardUsed({ShamblingGoblin.class, GrizzlyBears.class, LightningBolt.class})
class ShamblingGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("When Shambling Goblin dies, target opponent creature gets -1/-1")
    void deathTriggerDebuffsOpponentCreature() {
        harness.addToBattlefield(player1, new ShamblingGoblin());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        killGoblin();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(targetId);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Grizzly Bears");
        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Shambling Goblin's death trigger cannot target your creature")
    void deathTriggerOnlyTargetsOpponentCreatures() {
        harness.addToBattlefield(player1, new ShamblingGoblin());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID ownCreatureId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opponentCreatureId = harness.getPermanentId(player2, "Grizzly Bears");

        killGoblin();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(opponentCreatureId)
                .doesNotContain(ownCreatureId);
    }

    @Test
    @DisplayName("The -1/-1 effect wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ShamblingGoblin());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        killGoblin();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears").getPowerModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Grizzly Bears");
        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The death trigger puts a creature with zero toughness into its owner's graveyard")
    void deathTriggerKillsOneToughnessCreature() {
        harness.addToBattlefield(player1, new ShamblingGoblin());
        harness.addToBattlefield(player2, new ShamblingGoblin());
        UUID targetId = harness.getPermanentId(player2, "Shambling Goblin");

        killGoblin();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Shambling Goblin");
        harness.assertInGraveyard(player1, "Shambling Goblin");
        harness.assertInGraveyard(player2, "Shambling Goblin");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The death trigger is skipped when only your creatures remain")
    void deathTriggerHasNoLegalTargets() {
        harness.addToBattlefield(player1, new ShamblingGoblin());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killGoblin();

        harness.assertInGraveyard(player1, "Shambling Goblin");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        Permanent ownCreature = findPermanent(player1, "Grizzly Bears");
        assertThat(ownCreature.getPowerModifier()).isZero();
        assertThat(ownCreature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The death trigger does not affect another creature when its target leaves")
    void removedTargetDoesNotRedirectDebuff() {
        harness.addToBattlefield(player1, new ShamblingGoblin());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killGoblin();
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(survivor);
        assertThat(survivor.getPowerModifier()).isZero();
        assertThat(survivor.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void killGoblin() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID goblinId = harness.getPermanentId(player1, "Shambling Goblin");
        harness.castInstant(player2, 0, goblinId);
        harness.passBothPriorities();
    }
}
