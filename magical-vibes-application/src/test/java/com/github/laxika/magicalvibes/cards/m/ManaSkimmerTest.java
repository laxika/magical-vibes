package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaSkimmer.class, Forest.class})
class ManaSkimmerTest extends BaseCardTest {

    @Test
    @DisplayName("Damage to a player offers only that player's lands and locks the chosen land")
    void damagesPlayerAndLocksChosenLand() {
        Permanent skimmer = addCreatureReady(player1, new ManaSkimmer());
        Permanent damagedLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        skimmer.setAttacking(true);

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(damagedLand.getId());

        harness.handlePermanentChosen(player1, damagedLand.getId());
        harness.passBothPriorities();

        assertThat(damagedLand.isTapped()).isTrue();
        assertThat(damagedLand.getSkipUntapCount()).isEqualTo(1);
        assertThat(ownLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability resolves without a choice when the damaged player controls no lands")
    void resolvesWithoutChoiceWhenDamagedPlayerControlsNoLand() {
        Permanent skimmer = addCreatureReady(player1, new ManaSkimmer());
        skimmer.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The chosen land stays tapped through its next untap step")
    void chosenLandStaysTappedThroughNextUntapStep() {
        Permanent skimmer = addCreatureReady(player1, new ManaSkimmer());
        Permanent damagedLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        skimmer.setAttacking(true);

        resolveCombat();
        harness.handlePermanentChosen(player1, damagedLand.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        assertThat(damagedLand.isTapped()).isTrue();
        assertThat(damagedLand.getSkipUntapCount()).isZero();
    }
}
