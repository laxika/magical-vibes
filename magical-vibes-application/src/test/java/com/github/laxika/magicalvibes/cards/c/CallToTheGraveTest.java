package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.x.XantidSwarm;
import com.github.laxika.magicalvibes.cards.z.ZombieCutthroat;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CallToTheGrave.class, XantidSwarm.class, ZombieCutthroat.class})
class CallToTheGraveTest extends BaseCardTest {

    @Test
    @DisplayName("Controller sacrifices a non-Zombie creature at their own upkeep")
    void controllerSacrificesAtOwnUpkeep() {
        harness.addToBattlefield(player1, new CallToTheGrave());
        harness.addToBattlefield(player1, new XantidSwarm());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Xantid Swarm");
        harness.assertInGraveyard(player1, "Xantid Swarm");
    }

    @Test
    @DisplayName("Opponent sacrifices their own creature at their upkeep")
    void opponentSacrificesAtTheirUpkeep() {
        harness.addToBattlefield(player1, new CallToTheGrave());
        harness.addToBattlefield(player1, new XantidSwarm());
        harness.addToBattlefield(player2, new XantidSwarm());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Xantid Swarm");
        harness.assertOnBattlefield(player1, "Xantid Swarm");
    }

    @Test
    @DisplayName("Zombies are not eligible to be sacrificed")
    void zombiesAreNotSacrificed() {
        harness.addToBattlefield(player1, new CallToTheGrave());
        harness.addToBattlefield(player1, new ZombieCutthroat());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Zombie Cutthroat");
    }

    @Test
    @DisplayName("With multiple non-Zombie creatures the player chooses which one to sacrifice")
    void playerChoosesWhichNonZombieToSacrifice() {
        harness.addToBattlefield(player1, new CallToTheGrave());
        Permanent insect = harness.addToBattlefieldAndReturn(player1, new XantidSwarm());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new XantidSwarm());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new ZombieCutthroat());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.playerId()).isEqualTo(player1.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(insect.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(insect.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(other.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(zombie.getId()));
    }

    @Test
    @DisplayName("Sacrifices itself at the end step when no creatures are on the battlefield")
    void sacrificesSelfAtEndStepWithNoCreatures() {
        harness.addToBattlefield(player1, new CallToTheGrave());

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Call to the Grave");
        harness.assertInGraveyard(player1, "Call to the Grave");
    }

    @Test
    @DisplayName("Survives the end step while a creature is on the battlefield")
    void survivesEndStepWithCreaturePresent() {
        harness.addToBattlefield(player1, new CallToTheGrave());
        harness.addToBattlefield(player2, new XantidSwarm());

        harness.passUntil(player1, TurnStep.END_STEP);

        // The intervening-if fails (a creature is present), so no self-sacrifice trigger is put on
        // the stack during the end step and the enchantment survives it.
        harness.assertOnBattlefield(player1, "Call to the Grave");
        harness.assertNotInGraveyard(player1, "Call to the Grave");
    }

    @Test
    @DisplayName("Does not sacrifice itself if a creature appears before the end-step trigger resolves")
    void doesNotSacrificeIfCreatureAppearsBeforeTriggerResolves() {
        harness.addToBattlefield(player1, new CallToTheGrave());

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.addToBattlefield(player2, new XantidSwarm());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Call to the Grave");
        harness.assertNotInGraveyard(player1, "Call to the Grave");
    }
}
