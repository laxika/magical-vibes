package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Levitation.class, RuneclawBear.class, Opalescence.class})
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
        Permanent bear = addCreatureReady(player1, new RuneclawBear());
        harness.addToBattlefield(player1, new Levitation());

        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering under your control gain flying")
    void laterCreaturesGainFlying() {
        harness.addToBattlefield(player1, new Levitation());
        Permanent bear = harness.enterBattlefieldAndReturn(player1, new RuneclawBear());

        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Levitation gains flying when it becomes a creature")
    void levitationGainsFlyingWhenItBecomesCreature() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent levitation = harness.addToBattlefieldAndReturn(player1, new Levitation());

        assertThat(gqs.isCreature(gd, levitation)).isTrue();
        assertThat(gqs.hasKeyword(gd, levitation, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Opponent creatures do not gain flying")
    void opponentCreaturesDoNotGainFlying() {
        Permanent opponentBear = addCreatureReady(player2, new RuneclawBear());
        harness.addToBattlefield(player1, new Levitation());

        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Noncreatures you control do not gain flying")
    void noncreaturesDoNotGainFlying() {
        Permanent levitation = harness.addToBattlefieldAndReturn(player1, new Levitation());

        assertThat(gqs.isCreature(gd, levitation)).isFalse();
        assertThat(gqs.hasKeyword(gd, levitation, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying bonus is removed when Levitation leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent bear = addCreatureReady(player1, new RuneclawBear());
        Permanent levitation = harness.addToBattlefieldAndReturn(player1, new Levitation());
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, levitation));

        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying remains until the last Levitation leaves the battlefield")
    void flyingRemainsWhileAnotherLevitationIsPresent() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Levitation());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Levitation());

        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first));

        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, second));

        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isFalse();
    }
}
