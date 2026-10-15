package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.e.ExpeditionEnvoy;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LanternScout.class, GrizzlyBears.class, ExpeditionEnvoy.class, Conspiracy.class})
class LanternScoutTest extends BaseCardTest {

    @Test
    void allyEntryGivesLifelinkToAllYourCreatures() {
        Permanent nonAlly = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new LanternScout(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Lantern Scout"), Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAlly, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void nonAllyEntryDoesNotTrigger() {
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new LanternScout());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.hasKeyword(gd, scout, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void grantedLifelinkWearsOffAtEndOfTurn() {
        harness.castFromHand(player1, new LanternScout(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent scout = findPermanent(player1, "Lantern Scout");
        assertThat(gqs.hasKeyword(gd, scout, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, scout, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void anotherAllyGrantsLifelinkOnlyToYourCreatures() {
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new LanternScout());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new ExpeditionEnvoy());

        harness.castFromHand(player1, new ExpeditionEnvoy(), "{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, scout, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Expedition Envoy"), Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void opponentsAllyEntryDoesNotTrigger() {
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new LanternScout());
        gd.activePlayerId = player2.getId();

        harness.castFromHand(player2, new ExpeditionEnvoy(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, scout, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void creaturesEnteringAfterRallyResolvesDoNotGainLifelink() {
        harness.castFromHand(player1, new LanternScout(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Lantern Scout"), Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.LIFELINK)).isFalse();
    }

    @Test
    void ownEntryTriggersEvenWhenCreatureTypesAreReplaced() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);

        harness.castFromHand(player1, new LanternScout(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Lantern Scout"), Keyword.LIFELINK)).isTrue();
    }
}
