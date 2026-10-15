package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SeaGateLoremaster;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MakindiPatrol.class, GrizzlyBears.class, SeaGateLoremaster.class, Conspiracy.class})
class MakindiPatrolTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry gives your creatures vigilance")
    void ownAllyEntryGrantsVigilanceToYourCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new MakindiPatrol(), "{2}{W}");
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
        harness.castFromHand(player1, new SeaGateLoremaster(), "{4}{U}");
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
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, patrol, Keyword.VIGILANCE)).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Granted vigilance wears off at end of turn")
    void vigilanceWearsOffAtEndOfTurn() {
        harness.castFromHand(player1, new MakindiPatrol(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent patrol = findPermanent(player1, "Makindi Patrol");
        assertThat(gqs.hasKeyword(gd, patrol, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, patrol, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Its own entry triggers even when its Ally type has been replaced")
    void ownEntryTriggersWithoutAllyType() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);
        harness.castFromHand(player1, new MakindiPatrol(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent patrol = findPermanent(player1, "Makindi Patrol");
        assertThat(gqs.hasKeyword(gd, patrol, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's Ally entry neither triggers nor grants vigilance to your creatures")
    void opponentsAllyEntryDoesNotTrigger() {
        Permanent patrol = harness.addToBattlefieldAndReturn(player1, new MakindiPatrol());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new MakindiPatrol(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, patrol, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Makindi Patrol"), Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after Rally resolves do not receive its vigilance")
    void laterCreatureDoesNotReceiveVigilance() {
        harness.castFromHand(player1, new MakindiPatrol(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Makindi Patrol"), Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Rally grants vigilance to creatures present at resolution, including new arrivals")
    void creaturesEnteringBeforeResolutionReceiveVigilance() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new MakindiPatrol(), "{2}{W}");
        harness.passBothPriorities();
        Permanent patrol = findPermanent(player1, "Makindi Patrol");
        assertThat(gqs.hasKeyword(gd, patrol, Keyword.VIGILANCE)).isFalse();

        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, patrol, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.VIGILANCE)).isFalse();
    }
}
