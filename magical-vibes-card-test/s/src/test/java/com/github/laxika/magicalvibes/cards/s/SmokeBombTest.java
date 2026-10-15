package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SmokeBomb.class, GrizzlyBears.class, Smother.class, MarchOfTheMachines.class})
class SmokeBombTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures have shroud")
    void allCreaturesHaveShroud() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new SmokeBomb());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Shroud prevents targeting any creature")
    void shroudPreventsTargeting() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new SmokeBomb());
        harness.setHand(player1, List.of(new Smother()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Upkeep sacrifice queues an unblockable target creature you control")
    void upkeepSacrificeQueuesUnblockableTarget() {
        Permanent smokeBomb = harness.addToBattlefieldAndReturn(player1, new SmokeBomb());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, smokeBomb.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, ownCreature)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, opposingCreature)).isFalse();
        harness.assertInGraveyard(player1, "Smoke Bomb");

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, ownCreature)).isFalse();
    }

    @Test
    @DisplayName("An animated Smoke Bomb grants itself shroud")
    void animatedSmokeBombHasShroud() {
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        Permanent smokeBomb = harness.addToBattlefieldAndReturn(player1, new SmokeBomb());

        assertThat(gqs.hasKeyword(gd, smokeBomb, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Another Smoke Bomb prevents choosing a reflexive target")
    void anotherSmokeBombLeavesNoLegalTarget() {
        Permanent smokeBomb = harness.addToBattlefieldAndReturn(player1, new SmokeBomb());
        harness.addToBattlefield(player2, new SmokeBomb());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, smokeBomb.getId());

        harness.assertInGraveyard(player1, "Smoke Bomb");
        harness.assertOnBattlefield(player2, "Smoke Bomb");
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.SHROUD)).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasCantBeBlocked(gd, ownCreature)).isFalse();
    }

    @Test
    @DisplayName("Smoke Bomb is sacrificed even with no creature you control")
    void sacrificeWithoutOwnCreatures() {
        Permanent smokeBomb = harness.addToBattlefieldAndReturn(player1, new SmokeBomb());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, smokeBomb.getId());

        harness.assertInGraveyard(player1, "Smoke Bomb");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, opposingCreature)).isFalse();
    }

    @Test
    @DisplayName("Smoke Bomb does not trigger on an opponent's upkeep")
    void opponentsUpkeepDoesNotSacrificeSmokeBomb() {
        harness.addToBattlefield(player1, new SmokeBomb());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Smoke Bomb");
    }

    @Test
    @DisplayName("Smoke Bomb can be cast during an opponent's upkeep")
    void flashAllowsCastingOnOpponentsTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        advanceToUpkeep(player2);
        harness.setHand(player1, List.of(new SmokeBomb()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Smoke Bomb");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
    }
}
