package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TombstoneStairwell.class, IronTuskElephant.class, Disenchant.class})
class TombstoneStairwellTest extends BaseCardTest {

    /** Advances to player2's upkeep so only the each-upkeep trigger fires (no cumulative upkeep prompt). */
    private void advanceToOpponentUpkeep() {
        advanceToUpkeep(player2);
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Each player creates a Tombspawn for each creature card in their own graveyard")
    void createsTombspawnPerCreatureCardInGraveyard() {
        harness.addToBattlefield(player1, new TombstoneStairwell());
        harness.setGraveyard(player1, List.of(new IronTuskElephant(), new IronTuskElephant(), new Disenchant()));
        harness.setGraveyard(player2, List.of(new IronTuskElephant()));

        advanceToOpponentUpkeep();

        assertThat(countPermanents(player1, "Tombspawn")).isEqualTo(2);
        assertThat(countPermanents(player2, "Tombspawn")).isEqualTo(1);
    }

    @Test
    @DisplayName("Tombspawn tokens are 2/2 black Zombies with haste")
    void tombspawnHasCorrectCharacteristics() {
        harness.addToBattlefield(player1, new TombstoneStairwell());
        harness.setGraveyard(player1, List.of(new IronTuskElephant()));

        advanceToOpponentUpkeep();

        Permanent tombspawn = findPermanents(player1, "Tombspawn").getFirst();
        assertThat(tombspawn.getEffectivePower()).isEqualTo(2);
        assertThat(tombspawn.getEffectiveToughness()).isEqualTo(2);
        assertThat(tombspawn.getCard().getColors()).containsExactly(CardColor.BLACK);
        assertThat(tombspawn.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(gqs.hasKeyword(gd, tombspawn, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A player with no creature cards in their graveyard creates nothing")
    void noCreatureCardsNoTokens() {
        harness.addToBattlefield(player1, new TombstoneStairwell());
        harness.setGraveyard(player1, List.of(new Disenchant()));

        advanceToOpponentUpkeep();

        assertThat(countPermanents(player1, "Tombspawn")).isZero();
        assertThat(countPermanents(player2, "Tombspawn")).isZero();
    }

    @Test
    @DisplayName("All Tombspawn tokens are destroyed at the beginning of the end step")
    void destroysTokensAtEndStep() {
        harness.addToBattlefield(player1, new TombstoneStairwell());
        harness.setGraveyard(player1, List.of(new IronTuskElephant(), new IronTuskElephant()));
        harness.setGraveyard(player2, List.of(new IronTuskElephant()));

        advanceToOpponentUpkeep();
        assertThat(countPermanents(player1, "Tombspawn")).isEqualTo(2);

        advanceToEndStep();

        assertThat(countPermanents(player1, "Tombspawn")).isZero();
        assertThat(countPermanents(player2, "Tombspawn")).isZero();
    }

    @Test
    @DisplayName("Tokens are destroyed when Tombstone Stairwell leaves the battlefield")
    void destroysTokensWhenStairwellLeaves() {
        harness.addToBattlefield(player1, new TombstoneStairwell());
        harness.setGraveyard(player1, List.of(new IronTuskElephant()));
        harness.setGraveyard(player2, List.of(new IronTuskElephant()));

        advanceToOpponentUpkeep();
        assertThat(countPermanents(player1, "Tombspawn")).isEqualTo(1);
        assertThat(countPermanents(player2, "Tombspawn")).isEqualTo(1);

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, findPermanents(player1, "Tombstone Stairwell").getFirst().getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tombstone Stairwell");
        assertThat(countPermanents(player1, "Tombspawn")).isZero();
        assertThat(countPermanents(player2, "Tombspawn")).isZero();
    }

    @Test
    @DisplayName("Paying cumulative upkeep adds an age counter and keeps Tombstone Stairwell")
    void paysCumulativeUpkeep() {
        Permanent stairwell = harness.addToBattlefieldAndReturn(player1, new TombstoneStairwell());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(stairwell.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(stairwell);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Tombstone Stairwell")
    void decliningCumulativeUpkeepSacrifices() {
        Permanent stairwell = harness.addToBattlefieldAndReturn(player1, new TombstoneStairwell());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(stairwell);
        harness.assertInGraveyard(player1, "Tombstone Stairwell");
        resolveAllTriggers();
    }

    @Test
    @DisplayName("The each-upkeep ability does nothing if Tombstone Stairwell leaves before it resolves")
    void eachUpkeepAbilityDoesNothingAfterStairwellLeaves() {
        Permanent stairwell = harness.addToBattlefieldAndReturn(player1, new TombstoneStairwell());
        harness.setGraveyard(player2, List.of(new IronTuskElephant()));

        advanceToUpkeep(player2);

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, stairwell.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Tombstone Stairwell");
        assertThat(countPermanents(player1, "Tombspawn")).isZero();
        assertThat(countPermanents(player2, "Tombspawn")).isZero();
    }
}
