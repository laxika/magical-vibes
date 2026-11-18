package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CliffThreader;
import com.github.laxika.magicalvibes.cards.s.StoneworkPuma;
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

@CardUsed({HighlandBerserker.class, StoneworkPuma.class, CliffThreader.class})
class HighlandBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry may give first strike to all Allies you control")
    void ownAllyEntryMayGrantFirstStrikeToAllAllies() {
        Permanent existingAlly = harness.addToBattlefieldAndReturn(player1, new StoneworkPuma());
        Permanent opposingAlly = harness.addToBattlefieldAndReturn(player2, new StoneworkPuma());
        Permanent nonAlly = harness.addToBattlefieldAndReturn(player1, new CliffThreader());
        harness.setHand(player1, List.of(new HighlandBerserker()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent enteringAlly = findPermanent(player1, "Highland Berserker");
        assertThat(gqs.hasKeyword(gd, existingAlly, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, enteringAlly, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAlly, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingAlly, Keyword.FIRST_STRIKE)).isFalse();
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
        assertThat(gqs.hasKeyword(gd, existingBerserker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, enteringBerserker, Keyword.FIRST_STRIKE)).isTrue();
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

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Highland Berserker"), Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A non-Ally creature entering does not trigger it")
    void nonAllyEntryDoesNotTrigger() {
        Permanent berserker = harness.addToBattlefieldAndReturn(player1, new HighlandBerserker());
        harness.setHand(player1, List.of(new CliffThreader()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.hasKeyword(gd, berserker, Keyword.FIRST_STRIKE)).isFalse();
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
        assertThat(gqs.hasKeyword(gd, berserker, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, berserker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's Ally entering does not trigger Highland Berserker")
    void opposingAllyEntryDoesNotTrigger() {
        Permanent berserker = harness.addToBattlefieldAndReturn(player1, new HighlandBerserker());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new StoneworkPuma()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.hasKeyword(gd, berserker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The source must still be controlled by the ability's controller to receive first strike")
    void sourceChangingControllerBeforeResolutionDoesNotReceiveFirstStrike() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new StoneworkPuma());
        harness.setHand(player1, List.of(new HighlandBerserker()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent berserker = findPermanent(player1, "Highland Berserker");
        gd.playerBattlefields.get(player1.getId()).remove(berserker);
        gd.playerBattlefields.get(player2.getId()).add(berserker);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.hasKeyword(gd, ally, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, berserker, Keyword.FIRST_STRIKE)).isFalse();
    }
}
