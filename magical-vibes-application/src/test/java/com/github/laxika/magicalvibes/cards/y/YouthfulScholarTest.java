package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YouthfulScholar.class, Shock.class})
class YouthfulScholarTest extends BaseCardTest {

    @Test
    @DisplayName("When Youthful Scholar dies, its controller draws two cards")
    void diesDrawsTwoCards() {
        Permanent scholar = harness.addToBattlefieldAndReturn(player1, new YouthfulScholar());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveInstant(player2, 0, scholar.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        harness.assertInGraveyard(player1, "Youthful Scholar");
    }

    @Test
    @DisplayName("Sacrificing Youthful Scholar draws only when its death trigger resolves")
    void sacrificeDrawWaitsForResolution() {
        Permanent scholar = harness.addToBattlefieldAndReturn(player1, new YouthfulScholar());
        harness.setLibrary(player1, List.of(new YouthfulScholar(), new YouthfulScholar()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, scholar);

        harness.assertInGraveyard(player1, "Youthful Scholar");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A stolen Youthful Scholar draws for its controller rather than its owner")
    void stolenScholarDrawsForController() {
        Permanent scholar = harness.addToBattlefieldAndReturn(player2, new YouthfulScholar());
        gd.stolenCreatures.put(scholar.getId(), player1.getId());
        harness.setLibrary(player2, List.of(new YouthfulScholar(), new YouthfulScholar()));
        int ownerHandSize = gd.playerHands.get(player1.getId()).size();
        int controllerHandSize = gd.playerHands.get(player2.getId()).size();

        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, scholar);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Youthful Scholar");
        harness.assertNotInGraveyard(player2, "Youthful Scholar");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(ownerHandSize);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(controllerHandSize + 2);
    }

    @Test
    @DisplayName("Exiling Youthful Scholar does not trigger a draw")
    void exileDoesNotDraw() {
        Permanent scholar = harness.addToBattlefieldAndReturn(player1, new YouthfulScholar());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.getPermanentRemovalService().removePermanentToExile(gd, scholar);

        harness.assertNotOnBattlefield(player1, "Youthful Scholar");
        harness.assertNotInGraveyard(player1, "Youthful Scholar");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }
}
