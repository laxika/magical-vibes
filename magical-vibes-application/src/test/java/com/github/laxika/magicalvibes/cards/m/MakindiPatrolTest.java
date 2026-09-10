package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SeaGateLoremaster;
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

@CardUsed({MakindiPatrol.class, GrizzlyBears.class, SeaGateLoremaster.class})
class MakindiPatrolTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry gives your creatures vigilance")
    void ownAllyEntryGrantsVigilanceToYourCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MakindiPatrol()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent patrol = findPermanent(player1, "Makindi Patrol");
        assertThat(gqs.hasKeyword(gd, patrol, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Another Ally entry gives all your creatures vigilance")
    void anotherAllyEntryGrantsVigilanceToYourCreatures() {
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new MakindiPatrol());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeaGateLoremaster()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent ally = findPermanent(player1, "Sea Gate Loremaster");
        assertThat(gqs.hasKeyword(gd, patrol, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("A non-Ally creature entry does not trigger Makindi Patrol")
    void nonAllyEntryDoesNotTrigger() {
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new MakindiPatrol());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, patrol, Keyword.VIGILANCE)).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Granted vigilance wears off at end of turn")
    void vigilanceWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new MakindiPatrol()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent patrol = findPermanent(player1, "Makindi Patrol");
        assertThat(gqs.hasKeyword(gd, patrol, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, patrol, Keyword.VIGILANCE)).isFalse();
    }
}
