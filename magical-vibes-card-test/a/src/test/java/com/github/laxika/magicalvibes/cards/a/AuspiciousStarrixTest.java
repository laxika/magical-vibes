package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AuspiciousStarrix.class, Forest.class, GrizzlyBears.class, Shock.class, Pacifism.class})
class AuspiciousStarrixTest extends BaseCardTest {

    @Test
    @DisplayName("A mutation exiles until one permanent is found and puts it onto the battlefield")
    void mutationFindsOnePermanent() {
        Permanent starrix = addCreatureReady(player1, new AuspiciousStarrix());
        Card shock = new Shock();
        Card forest = new Forest();
        Card remainingShock = new Shock();
        harness.setLibrary(player1, List.of(shock, forest, remainingShock));

        triggerMutation(starrix);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(forest.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(shock.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(remainingShock);
    }

    @Test
    @DisplayName("The number of permanents found scales with the mutation count")
    void mutationCountControlsPermanentCount() {
        Permanent starrix = addCreatureReady(player1, new AuspiciousStarrix());
        Card shockOne = new Shock();
        Card forestOne = new Forest();
        Card shockTwo = new Shock();
        Card bears = new GrizzlyBears();
        Card forestTwo = new Forest();
        harness.setLibrary(player1, List.of(shockOne, forestOne, shockTwo, bears, forestTwo));

        triggerMutationWithoutResolving(starrix);
        triggerMutationWithoutResolving(starrix);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(starrix.getCard().getId(), forestOne.getId(), bears.getId(), forestTwo.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(shockOne.getId(), shockTwo.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void pendingTriggersUseMutationCountAtResolution() {
        Permanent starrix = addCreatureReady(player1, new AuspiciousStarrix());
        Card forestOne = new Forest();
        Card forestTwo = new Forest();
        Card forestThree = new Forest();
        Card forestFour = new Forest();
        harness.setLibrary(player1, List.of(forestOne, forestTwo, forestThree, forestFour));

        triggerMutationWithoutResolving(starrix);
        triggerMutationWithoutResolving(starrix);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(starrix.getCard().getId(), forestOne.getId(),
                        forestTwo.getId(), forestThree.getId(), forestFour.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyLibraryDoesNothing() {
        Permanent starrix = addCreatureReady(player1, new AuspiciousStarrix());
        harness.setLibrary(player1, List.of());

        triggerMutation(starrix);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(starrix);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void auraAttachesToExistingCreatureWithoutBeingCast() {
        Permanent starrix = addCreatureReady(player1, new AuspiciousStarrix());
        Card pacifism = new Pacifism();
        Card remainingForest = new Forest();
        harness.setLibrary(player1, List.of(pacifism, remainingForest));

        triggerMutation(starrix);

        assertThat(findPermanent(player1, "Pacifism").getAttachedTo()).isEqualTo(starrix.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingForest);
    }

    @Test
    void unattachableAuraRemainsExiledAndCountsAsPermanent() {
        Permanent starrix = addCreatureReady(player1, new AuspiciousStarrix());
        Card pacifism = new Pacifism();
        Card remainingForest = new Forest();
        harness.setLibrary(player1, List.of(pacifism, remainingForest));

        triggerMutationWithoutResolving(starrix);
        gd.playerBattlefields.get(player1.getId()).remove(starrix);
        gd.playerGraveyards.get(player1.getId()).add(starrix.getCard());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(pacifism);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingForest);
    }

    @Test
    void auraCannotEnchantCreatureEnteringInSameBatch() {
        Permanent starrix = addCreatureReady(player1, new AuspiciousStarrix());
        Card enteringStarrix = new AuspiciousStarrix();
        Card pacifism = new Pacifism();
        harness.setLibrary(player1, List.of(enteringStarrix, pacifism));

        triggerMutationWithoutResolving(starrix);
        triggerMutationWithoutResolving(starrix);
        gd.playerBattlefields.get(player1.getId()).remove(starrix);
        gd.playerGraveyards.get(player1.getId()).add(starrix.getCard());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(enteringStarrix.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(pacifism);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void insufficientPermanentsExilesEntireLibrary() {
        Permanent starrix = addCreatureReady(player1, new AuspiciousStarrix());
        Card forest = new Forest();
        Card trailingInstant = new Shock();
        harness.setLibrary(player1, List.of(forest, trailingInstant));

        triggerMutationWithoutResolving(starrix);
        triggerMutationWithoutResolving(starrix);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(starrix.getCard().getId(), forest.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(trailingInstant);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void triggerMutation(Permanent starrix) {
        triggerMutationWithoutResolving(starrix);
        resolveAllTriggers();
    }

    private void triggerMutationWithoutResolving(Permanent starrix) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, starrix, List.of(starrix.getCard()), player1.getId()));
    }
}
