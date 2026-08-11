package com.github.laxika.magicalvibes.cards.s;

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

class SeascapeAerialistTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry may give flying to all Allies you control")
    void ownAllyEntryMayGrantFlyingToAllAllies() {
        Permanent existingAlly = harness.addToBattlefieldAndReturn(player1, new KazanduBlademaster());
        Permanent nonAlly = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeascapeAerialist()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent enteringAlly = findPermanent(player1, "Seascape Aerialist");
        assertThat(existingAlly.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(enteringAlly.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(nonAlly.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Another Ally entering may give flying to all Allies you control")
    void anotherAllyEntryMayGrantFlyingToAllAllies() {
        Permanent existingAerialist = harness.addToBattlefieldAndReturn(player1, new SeascapeAerialist());
        harness.setHand(player1, List.of(new SeascapeAerialist()));
        harness.addMana(player1, ManaColor.BLUE, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent enteringAerialist = findPermanent(player1, "Seascape Aerialist");
        assertThat(existingAerialist.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(enteringAerialist.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Declining the triggered ability grants no flying")
    void mayBeDeclined() {
        harness.setHand(player1, List.of(new SeascapeAerialist()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Seascape Aerialist").hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A non-Ally creature entering does not trigger it")
    void nonAllyEntryDoesNotTrigger() {
        Permanent aerialist = harness.addToBattlefieldAndReturn(player1, new SeascapeAerialist());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(aerialist.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new SeascapeAerialist()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent aerialist = findPermanent(player1, "Seascape Aerialist");
        assertThat(aerialist.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(aerialist.hasKeyword(Keyword.FLYING)).isFalse();
    }
}
