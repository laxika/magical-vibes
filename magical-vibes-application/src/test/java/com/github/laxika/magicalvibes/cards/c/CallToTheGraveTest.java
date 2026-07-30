package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CallToTheGraveTest extends BaseCardTest {

    private static Card zombieCreature() {
        Card card = new Card();
        card.setName("Test Zombie");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.BLACK);
        card.setSubtypes(List.of(CardSubtype.ZOMBIE));
        card.setPower(1);
        card.setToughness(1);
        return card;
    }

    @Test
    @DisplayName("Controller sacrifices a non-Zombie creature at their own upkeep")
    void controllerSacrificesAtOwnUpkeep() {
        harness.addToBattlefield(player1, new CallToTheGrave());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Opponent sacrifices their own creature at their upkeep")
    void opponentSacrificesAtTheirUpkeep() {
        harness.addToBattlefield(player1, new CallToTheGrave());
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(theirs.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(mine.getId()));
    }

    @Test
    @DisplayName("Zombies are not eligible to be sacrificed")
    void zombiesAreNotSacrificed() {
        harness.addToBattlefield(player1, new CallToTheGrave());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, zombieCreature());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(zombie.getId()));
    }

    @Test
    @DisplayName("With multiple non-Zombie creatures the player chooses which one to sacrifice")
    void playerChoosesWhichNonZombieToSacrifice() {
        harness.addToBattlefield(player1, new CallToTheGrave());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, zombieCreature());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.playerId()).isEqualTo(player1.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(other.getId()));
    }

    @Test
    @DisplayName("Sacrifices itself at the end step when no creatures are on the battlefield")
    void sacrificesSelfAtEndStepWithNoCreatures() {
        harness.addToBattlefield(player1, new CallToTheGrave());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Call to the Grave");
        harness.assertInGraveyard(player1, "Call to the Grave");
    }

    @Test
    @DisplayName("Survives the end step while a creature is on the battlefield")
    void survivesEndStepWithCreaturePresent() {
        harness.addToBattlefield(player1, new CallToTheGrave());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // The intervening-if fails (a creature is present), so no self-sacrifice trigger is put on
        // the stack during the end step and the enchantment survives it.
        harness.assertOnBattlefield(player1, "Call to the Grave");
        harness.assertNotInGraveyard(player1, "Call to the Grave");
    }
}
