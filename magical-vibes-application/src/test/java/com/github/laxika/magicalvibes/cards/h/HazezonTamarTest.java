package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HazezonTamar.class, Boomerang.class, Forest.class})
class HazezonTamarTest extends BaseCardTest {

    @Test
    void createsColoredSandWarriorsUsingLandCountAtNextUpkeep() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        castHazezon();
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Sand Warrior");
        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getSubtypes())
                    .containsExactlyInAnyOrder(CardSubtype.SAND, CardSubtype.WARRIOR);
            assertThat(token.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.RED, CardColor.GREEN, CardColor.WHITE);
        });
    }

    @Test
    void delayedCreationStillHappensIfHazezonLeavesBeforeNextUpkeep() {
        harness.addToBattlefield(player1, new Forest());
        Permanent hazezon = castHazezon();

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, hazezon.getId());
        resolveAllTriggers();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Sand Warrior")).isEqualTo(1);
    }

    @Test
    void leavingExilesAllSandWarriors() {
        harness.addToBattlefield(player1, new Forest());
        Permanent hazezon = castHazezon();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Sand Warrior")).isEqualTo(1);

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, hazezon.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Sand Warrior")).isZero();
    }

    private Permanent castHazezon() {
        harness.setHand(player1, List.of(new HazezonTamar()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Hazezon Tamar");
    }
}
