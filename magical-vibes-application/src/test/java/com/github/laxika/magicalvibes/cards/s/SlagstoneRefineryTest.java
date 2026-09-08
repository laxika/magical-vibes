package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlagstoneRefineryTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a tapped Powerstone when it is put into a graveyard")
    void createsPowerstoneWhenPutIntoGraveyard() {
        Permanent refinery = harness.addToBattlefieldAndReturn(player1, new SlagstoneRefinery());

        removeToGraveyard(refinery);

        assertThat(countPermanents(player1, "Powerstone")).isEqualTo(1);
        assertThat(findPermanents(player1, "Powerstone").getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creates a tapped Powerstone when it is exiled")
    void createsPowerstoneWhenExiled() {
        Permanent refinery = harness.addToBattlefieldAndReturn(player1, new SlagstoneRefinery());

        removeToExile(refinery);

        assertThat(countPermanents(player1, "Powerstone")).isEqualTo(1);
        assertThat(findPermanents(player1, "Powerstone").getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creates a Powerstone for another nontoken artifact you control going to a graveyard or exile")
    void createsPowerstoneForAnotherNontokenArtifact() {
        harness.addToBattlefield(player1, new SlagstoneRefinery());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        removeToGraveyard(artifact);
        removeToExile(harness.addToBattlefieldAndReturn(player1, new Spellbook()));

        assertThat(countPermanents(player1, "Powerstone")).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for tokens, opponent artifacts, or artifacts returned to hand")
    void ignoresNonMatchingArtifacts() {
        harness.addToBattlefield(player1, new SlagstoneRefinery());
        Card tokenCard = new Spellbook();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Permanent bouncedArtifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        removeToGraveyard(token);
        removeToGraveyard(opponentArtifact);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bouncedArtifact));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Powerstone")).isZero();
    }

    private void removeToGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }

    private void removeToExile(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, permanent));
        harness.passBothPriorities();
    }
}
