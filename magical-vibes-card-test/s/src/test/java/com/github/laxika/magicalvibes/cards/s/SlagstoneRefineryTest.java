package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FadeFromHistory;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlagstoneRefinery.class, Spellbook.class, FadeFromHistory.class})
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

    @Test
    @DisplayName("Both Refineries trigger for themselves and each other during simultaneous destruction")
    void eachRefinerySeesEverySimultaneouslyDestroyedArtifact() {
        harness.addToBattlefield(player1, new SlagstoneRefinery());
        harness.addToBattlefield(player1, new SlagstoneRefinery());
        harness.setHand(player1, List.of(new FadeFromHistory()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Slagstone Refinery");
        assertThat(countPermanents(player1, "Powerstone")).isEqualTo(4);
        assertThat(findPermanents(player1, "Powerstone")).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("A Refinery sees artifacts exiled simultaneously with itself")
    void seesAnotherArtifactDuringSimultaneousExile() {
        Permanent refinery = harness.addToBattlefieldAndReturn(player1, new SlagstoneRefinery());
        Permanent otherRefinery = harness.addToBattlefieldAndReturn(player1, new SlagstoneRefinery());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().performSimultaneousRemovals(
                gd, List.of(refinery, otherRefinery), () -> {
                    harness.getPermanentRemovalService().removePermanentToExile(gd, refinery);
                    harness.getPermanentRemovalService().removePermanentToExile(gd, otherRefinery);
                }));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Powerstone")).isEqualTo(4);
        assertThat(findPermanents(player1, "Powerstone")).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("A token copy of the Refinery still triggers for its own graveyard or exile movement")
    void tokenRefineryTriggersForItsOwnRemoval() {
        Card graveyardCopy = new SlagstoneRefinery();
        graveyardCopy.setToken(true);
        removeToGraveyard(harness.addToBattlefieldAndReturn(player1, graveyardCopy));
        Card exileCopy = new SlagstoneRefinery();
        exileCopy.setToken(true);
        removeToExile(harness.addToBattlefieldAndReturn(player1, exileCopy));

        assertThat(countPermanents(player1, "Powerstone")).isEqualTo(2);
        assertThat(findPermanents(player1, "Powerstone")).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Returning the Refinery itself to hand does not create a Powerstone")
    void doesNotTriggerForItsOwnReturnToHand() {
        Permanent refinery = harness.addToBattlefieldAndReturn(player1, new SlagstoneRefinery());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, refinery));
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
