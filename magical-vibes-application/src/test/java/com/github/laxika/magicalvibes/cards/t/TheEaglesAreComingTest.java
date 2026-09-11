package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheEaglesAreComing.class, GrizzlyBears.class})
class TheEaglesAreComingTest extends BaseCardTest {

    @Test
    void returnsOneOwnCreatureAndCreatesOneBirdSoldierAtNextUpkeep() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(target, false);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Bird Soldier")).isEmpty();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        List<Permanent> birds = findPermanents(player1, "Bird Soldier");
        assertThat(birds).hasSize(1);
        assertThat(birds.getFirst().getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.BIRD, CardSubtype.SOLDIER);
        assertThat(gqs.hasKeyword(gd, birds.getFirst(), Keyword.FLYING)).isTrue();
    }

    @Test
    void kickedSpellReturnsAnyNumberOfOwnCreaturesAndCreatesOneBirdPerCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheEaglesAreComing()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.getGameService().playCard(gd, player1, 0, 0, null, null,
                List.of(first.getId(), second.getId()), List.of(), false,
                null, null, null, null, null, true);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        harness.assertInHand(player1, "Grizzly Bears");

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bird Soldier")).hasSize(2);
    }

    @Test
    void createsBirdsAtTheNextUpkeepEvenIfItIsTheOpponents() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(target, false);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bird Soldier")).hasSize(1);
    }

    @Test
    void cannotTargetAnOpponentOwnedCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TheEaglesAreComing()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("own");
    }

    private void cast(Permanent target, boolean kicked) {
        harness.setHand(player1, List.of(new TheEaglesAreComing()));
        harness.addMana(player1, ManaColor.WHITE, kicked ? 3 : 1);
        harness.addMana(player1, ManaColor.COLORLESS, kicked ? 3 : 1);
        if (kicked) {
            harness.castKickedInstant(player1, 0, target.getId());
        } else {
            harness.castInstant(player1, 0, target.getId());
        }
        harness.passBothPriorities();
    }
}
