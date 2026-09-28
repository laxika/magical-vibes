package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConcordantCrossroads.class, BarbaryApes.class})
class ConcordantCrossroadsTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures have haste")
    void grantsHasteToAllCreatures() {
        harness.addToBattlefield(player1, new ConcordantCrossroads());
        Permanent ownApes = harness.addToBattlefieldAndReturn(player1, new BarbaryApes());
        Permanent opponentApes = harness.addToBattlefieldAndReturn(player2, new BarbaryApes());

        assertThat(gqs.hasKeyword(gd, ownApes, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentApes, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after Concordant Crossroads also have haste")
    void grantsHasteToLaterCreatures() {
        harness.addToBattlefield(player1, new ConcordantCrossroads());
        Permanent apes = harness.addToBattlefieldAndReturn(player2, new BarbaryApes());

        assertThat(gqs.hasKeyword(gd, apes, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Noncreatures do not gain haste")
    void doesNotGrantHasteToNoncreatures() {
        Permanent crossroads = harness.addToBattlefieldAndReturn(player1, new ConcordantCrossroads());

        assertThat(gqs.hasKeyword(gd, crossroads, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Creatures lose the granted haste when Concordant Crossroads leaves")
    void hasteEndsWhenSourceLeaves() {
        Permanent crossroads = harness.addToBattlefieldAndReturn(player1, new ConcordantCrossroads());
        Permanent apes = harness.addToBattlefieldAndReturn(player2, new BarbaryApes());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, crossroads));

        assertThat(gqs.hasKeyword(gd, apes, Keyword.HASTE)).isFalse();
    }
}
