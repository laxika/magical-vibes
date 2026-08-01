package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FirestormHellkiteTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep keeps Firestorm Hellkite")
    void paysCumulativeUpkeep() {
        Permanent hellkite = harness.addToBattlefieldAndReturn(player1, new FirestormHellkite());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(hellkite.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hellkite);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Firestorm Hellkite")
    void declineSacrifices() {
        Permanent hellkite = harness.addToBattlefieldAndReturn(player1, new FirestormHellkite());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hellkite);
        harness.assertInGraveyard(player1, "Firestorm Hellkite");
    }
}
