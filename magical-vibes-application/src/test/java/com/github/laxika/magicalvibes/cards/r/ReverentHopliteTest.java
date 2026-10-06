package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AlseidOfLifesBounty;
import com.github.laxika.magicalvibes.cards.d.DaxosBlessedByTheSun;
import com.github.laxika.magicalvibes.cards.s.SternDismissal;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReverentHoplite.class, AlseidOfLifesBounty.class, DaxosBlessedByTheSun.class,
        SternDismissal.class})
class ReverentHopliteTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates one Human Soldier for its own white mana symbol")
    void createsOneTokenFromItsOwnDevotion() {
        castReverentHoplite();

        assertThat(findPermanents(player1, "Human Soldier")).hasSize(1);
    }

    @Test
    @DisplayName("Entering the battlefield creates Human Soldiers equal to white devotion")
    void createsTokensEqualToWhiteDevotion() {
        harness.addToBattlefield(player1, new AlseidOfLifesBounty());

        castReverentHoplite();

        List<Permanent> tokens = findPermanents(player1, "Human Soldier");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes())
                    .containsExactly(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        });
    }

    @Test
    void countsEachWhiteSymbolAndIgnoresOpponentsPermanents() {
        harness.addToBattlefield(player1, new DaxosBlessedByTheSun());
        harness.addToBattlefield(player2, new DaxosBlessedByTheSun());

        castReverentHoplite();

        assertThat(findPermanents(player1, "Human Soldier")).hasSize(3);
        assertThat(findPermanents(player2, "Human Soldier")).isEmpty();
    }

    @Test
    void createsNoTokensWhenHopliteLeavesBeforeTriggerResolves() {
        harness.castFromHand(player1, new ReverentHoplite(), "{4}{W}");
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new SternDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, findPermanent(player1, "Reverent Hoplite").getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Reverent Hoplite");

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Human Soldier")).isEmpty();
    }

    @Test
    void triggerStillCreatesTokensFromRemainingDevotionAfterHopliteLeaves() {
        harness.addToBattlefield(player1, new DaxosBlessedByTheSun());
        harness.castFromHand(player1, new ReverentHoplite(), "{4}{W}");
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new SternDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, findPermanent(player1, "Reverent Hoplite").getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Reverent Hoplite");

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Human Soldier")).hasSize(2);
    }

    @Test
    void generatedWhiteTokensDoNotIncreaseDevotionForAnotherHoplite() {
        castReverentHoplite();
        assertThat(findPermanents(player1, "Human Soldier")).hasSize(1);

        castReverentHoplite();

        assertThat(findPermanents(player1, "Human Soldier")).hasSize(3);
    }

    private void castReverentHoplite() {
        harness.castFromHand(player1, new ReverentHoplite(), "{4}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
