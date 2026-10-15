package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.ElixirOfImmortality;
import com.github.laxika.magicalvibes.cards.t.TomeScour;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoneRattler.class, ElixirOfImmortality.class, TomeScour.class})
@DisplayName("Bone Rattler")
class BoneRattlerTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, it exiles itself and creates four Reassembling Skeleton token cards")
    void deathCreatesReassemblingSkeletonTokenCards() {
        resolveDeathTrigger();

        harness.assertNotInGraveyard(player1, "Bone Rattler");
        List<Card> skeletonCards = graveyardCardsNamed("Reassembling Skeleton");
        assertThat(skeletonCards).hasSize(4);
        assertThat(skeletonCards).allSatisfy(card -> {
            assertThat(card.isToken()).isTrue();
            assertThat(card.isTokenCard()).isTrue();
            assertThat(card.getManaCost()).isEqualTo("{1}{B}");
        });
    }

    @Test
    @DisplayName("A Reassembling Skeleton token card can return itself from the graveyard")
    void tokenCardReturnsToBattlefieldTapped() {
        resolveDeathTrigger();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int skeletonIndex = firstGraveyardIndexNamed("Reassembling Skeleton");
        harness.activateGraveyardAbility(player1, skeletonIndex);
        harness.passBothPriorities();

        Permanent skeleton = findPermanent(player1, "Reassembling Skeleton");
        assertThat(skeleton.isTapped()).isTrue();
        assertThat(graveyardCardsNamed("Reassembling Skeleton")).hasSize(3);
    }

    @Test
    @DisplayName("Being milled also exiles Bone Rattler and creates Skeleton cards")
    void millingCreatesSkeletonCards() {
        harness.setLibrary(player1, List.of(new BoneRattler()));
        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player1.getId());
        resolveAllTriggers();

        harness.assertNotInGraveyard(player1, "Bone Rattler");
        assertThat(gd.exiledCards).anySatisfy(exiled ->
                assertThat(exiled.card().getName()).isEqualTo("Bone Rattler"));
        assertThat(graveyardCardsNamed("Reassembling Skeleton")).hasSize(4);
    }

    @Test
    @DisplayName("Creating Skeleton cards waits for a separate reflexive trigger")
    void skeletonCardsAreCreatedBySeparateTrigger() {
        putRattlerIntoGraveyard();

        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Bone Rattler");
        assertThat(gd.exiledCards).anySatisfy(exiled ->
                assertThat(exiled.card().getName()).isEqualTo("Bone Rattler"));
        assertThat(graveyardCardsNamed("Reassembling Skeleton")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(graveyardCardsNamed("Reassembling Skeleton")).hasSize(4);
    }

    @Test
    @DisplayName("No Skeleton cards are created if Bone Rattler leaves the graveyard before resolution")
    void sourceMissingPreventsSkeletonCreation() {
        putRattlerIntoGraveyard();
        Card rattler = gd.playerGraveyards.get(player1.getId()).getFirst();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removeCardFromGraveyardById(gd, rattler.getId()));
        harness.setHand(player1, List.of(rattler));

        resolveAllTriggers();

        harness.assertInHand(player1, "Bone Rattler");
        assertThat(graveyardCardsNamed("Reassembling Skeleton")).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("A Skeleton token card persists after dying and can return again")
    void skeletonCardCanReturnRepeatedly() {
        resolveDeathTrigger();
        Card skeletonCard = graveyardCardsNamed("Reassembling Skeleton").getFirst();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateGraveyardAbility(player1, firstGraveyardIndexNamed("Reassembling Skeleton"));
        harness.passBothPriorities();

        Permanent skeleton = findPermanent(player1, "Reassembling Skeleton");
        skeleton.setMarkedDamage(1);
        harness.runStateBasedActions();

        assertThat(graveyardCardsNamed("Reassembling Skeleton")).hasSize(4).contains(skeletonCard);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateGraveyardAbility(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(skeletonCard));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Reassembling Skeleton").getCard()).isSameAs(skeletonCard);
        assertThat(findPermanent(player1, "Reassembling Skeleton").isTapped()).isTrue();
        assertThat(graveyardCardsNamed("Reassembling Skeleton")).hasSize(3);
    }

    @Test
    @DisplayName("Skeleton token cards can be shuffled from the graveyard into the library")
    void skeletonCardsCanMoveIntoLibrary() {
        resolveDeathTrigger();
        List<Card> skeletonCards = graveyardCardsNamed("Reassembling Skeleton");
        harness.addToBattlefield(player1, new ElixirOfImmortality());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.runStateBasedActions();

        assertThat(gd.playerDecks.get(player1.getId())).containsAll(skeletonCards);
        assertThat(graveyardCardsNamed("Reassembling Skeleton")).isEmpty();
    }

    private void resolveDeathTrigger() {
        putRattlerIntoGraveyard();
        resolveAllTriggers();
    }

    private void putRattlerIntoGraveyard() {
        harness.setLibrary(player1, List.of());
        Permanent rattler = harness.addToBattlefieldAndReturn(player1, new BoneRattler());
        rattler.setMarkedDamage(4);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Bone Rattler");
    }

    private List<Card> graveyardCardsNamed(String name) {
        return gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals(name))
                .toList();
    }

    private int firstGraveyardIndexNamed(String name) {
        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        for (int i = 0; i < graveyard.size(); i++) {
            if (graveyard.get(i).getName().equals(name)) {
                return i;
            }
        }
        throw new AssertionError("No graveyard card named " + name);
    }
}
