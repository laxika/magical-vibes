package com.github.laxika.magicalvibes.cards.j;

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

class JoragaBardTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry may give vigilance to all Allies you control")
    void ownAllyEntryMayGrantVigilanceToAllAllies() {
        Permanent existingAlly = harness.addToBattlefieldAndReturn(player1, new KazanduBlademaster());
        Permanent nonAlly = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new JoragaBard()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent enteringAlly = findPermanent(player1, "Joraga Bard");
        assertThat(existingAlly.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(enteringAlly.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(nonAlly.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Another Ally entering may give vigilance to all Allies you control")
    void anotherAllyEntryMayGrantVigilanceToAllAllies() {
        Permanent existingBard = harness.addToBattlefieldAndReturn(player1, new JoragaBard());
        harness.setHand(player1, List.of(new JoragaBard()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent enteringBard = findPermanent(player1, "Joraga Bard");
        assertThat(existingBard.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(enteringBard.hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Declining the triggered ability grants no vigilance")
    void mayBeDeclined() {
        harness.setHand(player1, List.of(new JoragaBard()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Joraga Bard").hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("A non-Ally creature entering does not trigger it")
    void nonAllyEntryDoesNotTrigger() {
        Permanent bard = harness.addToBattlefieldAndReturn(player1, new JoragaBard());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(bard.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Granted vigilance wears off at end of turn")
    void vigilanceWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new JoragaBard()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent bard = findPermanent(player1, "Joraga Bard");
        assertThat(bard.hasKeyword(Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bard.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }
}
