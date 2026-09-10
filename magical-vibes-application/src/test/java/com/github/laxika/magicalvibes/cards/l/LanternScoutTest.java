package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LanternScout.class, GrizzlyBears.class})
class LanternScoutTest extends BaseCardTest {

    @Test
    void allyEntryGivesLifelinkToAllYourCreatures() {
        Permanent nonAlly = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LanternScout()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Lantern Scout"), Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAlly, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void nonAllyEntryDoesNotTrigger() {
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new LanternScout());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.hasKeyword(gd, scout, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void grantedLifelinkWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new LanternScout()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent scout = findPermanent(player1, "Lantern Scout");
        assertThat(gqs.hasKeyword(gd, scout, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, scout, Keyword.LIFELINK)).isFalse();
    }
}
