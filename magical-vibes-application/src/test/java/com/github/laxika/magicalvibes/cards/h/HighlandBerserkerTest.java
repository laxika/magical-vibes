package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KazanduBlademaster;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HighlandBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry may give first strike to all Allies you control")
    void ownAllyEntryMayGrantFirstStrikeToAllAllies() {
        Permanent existingAlly = harness.addToBattlefieldAndReturn(player1, new KazanduBlademaster());
        Permanent nonAlly = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HighlandBerserker()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent enteringAlly = findPermanent(player1, "Highland Berserker");
        assertThat(existingAlly.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(enteringAlly.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(nonAlly.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Another Ally entering may give first strike to all Allies you control")
    void anotherAllyEntryMayGrantFirstStrikeToAllAllies() {
        Permanent existingBerserker = harness.addToBattlefieldAndReturn(player1, new HighlandBerserker());
        harness.setHand(player1, List.of(new HighlandBerserker()));
        harness.addMana(player1, ManaColor.RED, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent enteringBerserker = findPermanent(player1, "Highland Berserker");
        assertThat(existingBerserker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(enteringBerserker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Declining the triggered ability grants no first strike")
    void mayBeDeclined() {
        harness.setHand(player1, List.of(new HighlandBerserker()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Highland Berserker").hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A non-Ally creature entering does not trigger it")
    void nonAllyEntryDoesNotTrigger() {
        Permanent berserker = harness.addToBattlefieldAndReturn(player1, new HighlandBerserker());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(berserker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Granted first strike wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new HighlandBerserker()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent berserker = findPermanent(player1, "Highland Berserker");
        assertThat(berserker.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(berserker.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }
}
