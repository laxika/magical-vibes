package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FollowTheBodies.class, GrizzlyBears.class, Shock.class})
class FollowTheBodiesTest extends BaseCardTest {

    @Test
    void investigates() {
        castFollowTheBodies();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void gravestormCopiesForEachPermanentPutIntoGraveyardFromTheBattlefieldThisTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bearId);

        castFollowTheBodies();
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    private void castFollowTheBodies() {
        harness.castFromHand(player1, new FollowTheBodies(), "{2}{U}");
    }

    @Test
    void countsPermanentsThatDieInResponseToTheGravestormTrigger() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        castFollowTheBodies();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bearId);
        harness.assertInGraveyard(player2, "Grizzly Bears");

        harness.passBothPriorities();
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    void sacrificedClueTokensCountAndCluesCanBeUsedToDrawCards() {
        harness.setLibrary(player1, List.of(new FollowTheBodies()));
        castFollowTheBodies();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.passBothPriorities();
        harness.assertInHand(player1, "Follow the Bodies");

        castFollowTheBodies();
        harness.passBothPriorities();
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    void countsMultipleSacrificedCluesWithoutCountingResolvedSorceries() {
        harness.setLibrary(player1, List.of(new FollowTheBodies(), new FollowTheBodies()));
        for (int i = 0; i < 2; i++) {
            castFollowTheBodies();
            harness.passBothPriorities();
            harness.passBothPriorities();
        }
        assertThat(findPermanents(player1, "Clue")).hasSize(2);

        for (int i = 0; i < 2; i++) {
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        castFollowTheBodies();
        harness.passBothPriorities();
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(3);
    }
}
