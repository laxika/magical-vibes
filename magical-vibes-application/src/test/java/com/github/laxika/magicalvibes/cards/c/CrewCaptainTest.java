package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CrewCaptain.class)
class CrewCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Has indestructible during the turn it enters")
    void hasIndestructibleDuringEnteringTurn() {
        harness.setHand(player1, List.of(new CrewCaptain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent captain = findPermanent(player1, "Crew Captain");
        assertThat(gqs.hasKeyword(gd, captain, Keyword.INDESTRUCTIBLE)).isTrue();

        captain.setMarkedDamage(2);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(captain);
    }

    @Test
    @DisplayName("Loses indestructible after the turn it enters")
    void losesIndestructibleAfterEnteringTurn() {
        harness.setHand(player1, List.of(new CrewCaptain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent captain = findPermanent(player1, "Crew Captain");
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, captain, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
