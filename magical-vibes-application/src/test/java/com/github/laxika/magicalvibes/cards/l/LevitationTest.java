package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Levitation.class, GiantCockroach.class})
class LevitationTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Levitation puts it on the stack as an enchantment spell")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new Levitation(), "{2}{U}{U}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Creatures you control gain flying")
    void ownCreaturesGainFlying() {
        Permanent cockroach = addCreatureReady(player1, new GiantCockroach());
        harness.addToBattlefield(player1, new Levitation());

        assertThat(gqs.hasKeyword(gd, cockroach, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Opponent creatures do not gain flying")
    void opponentCreaturesDoNotGainFlying() {
        Permanent opponentCockroach = addCreatureReady(player2, new GiantCockroach());
        harness.addToBattlefield(player1, new Levitation());

        assertThat(gqs.hasKeyword(gd, opponentCockroach, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying bonus is removed when Levitation leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent cockroach = addCreatureReady(player1, new GiantCockroach());
        Permanent levitation = harness.addToBattlefieldAndReturn(player1, new Levitation());
        assertThat(gqs.hasKeyword(gd, cockroach, Keyword.FLYING)).isTrue();

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, levitation));

        assertThat(gqs.hasKeyword(gd, cockroach, Keyword.FLYING)).isFalse();
    }
}
