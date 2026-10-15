package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BruteForce;
import com.github.laxika.magicalvibes.cards.c.Calciderm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Pongify;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildPair.class, GrizzlyBears.class, WallOfEssence.class, HillGiant.class, Zombify.class,
        BruteForce.class, Calciderm.class, Pongify.class})
class WildPairTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a creature offers a search for an exact total power and toughness match")
    void castingCreatureOffersExactTotalMatch() {
        castWildPair();
        Card matchingCreature = new WallOfEssence();
        harness.setLibrary(player1, List.of(matchingCreature, new HillGiant()));
        castGrizzlyBears();

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCreature);

        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Wall of Essence")).isNotNull();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Uses the entering creature's current size when the trigger resolves")
    void usesCurrentPowerAndToughnessAtResolution() {
        castWildPair();
        Card matchingCreature = new Calciderm();
        harness.setLibrary(player1, List.of(matchingCreature));
        castGrizzlyBears();
        harness.passBothPriorities();

        Permanent grizzlyBears = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new BruteForce()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, grizzlyBears.getId());
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCreature);

        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Calciderm")).isNotNull();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the search leaves the library unchanged")
    void decliningSearchDoesNothing() {
        castWildPair();
        Card matchingCreature = new WallOfEssence();
        harness.setLibrary(player1, List.of(matchingCreature));
        castGrizzlyBears();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).contains(matchingCreature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == matchingCreature);
    }

    @Test
    @DisplayName("A creature returned from a graveyard does not trigger Wild Pair")
    void creatureReturnedFromGraveyardDoesNotTrigger() {
        castWildPair();
        Card returnedCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returnedCreature));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, returnedCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == returnedCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }


    @Test
    @DisplayName("An opponent's creature cast from hand does not trigger your Wild Pair")
    void opponentCastingCreatureDoesNotTrigger() {
        castWildPair();
        harness.setLibrary(player1, List.of(new WallOfEssence()));
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        resolveAllTriggers();

        assertThat(findPermanent(player2, "Grizzly Bears")).isNotNull();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature that leaves uses its size immediately before leaving")
    void usesLastBattlefieldSizeAfterCreatureLeaves() {
        castWildPair();
        Card matchingCreature = new Calciderm();
        harness.setLibrary(player1, List.of(matchingCreature, new WallOfEssence()));
        castGrizzlyBears();
        harness.passBothPriorities();
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new BruteForce(), new Pongify()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCreature);
        harness.handleCardChosen(player1, 0);
        assertThat(findPermanent(player1, "Calciderm")).isNotNull();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A searched creature enters untapped without triggering another search")
    void searchedCreatureDoesNotTriggerAnotherSearch() {
        castWildPair();
        Card matchingCreature = new WallOfEssence();
        harness.setLibrary(player1, List.of(matchingCreature, new GrizzlyBears()));
        castGrizzlyBears();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Wall of Essence").isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(matchingCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An accepted search may fail to find an available matching creature")
    void canFailToFindMatchingCreature() {
        castWildPair();
        Card matchingCreature = new WallOfEssence();
        harness.setLibrary(player1, List.of(matchingCreature));
        castGrizzlyBears();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(matchingCreature);
        assertThat(countPermanents(player1, "Wall of Essence")).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An accepted search with no matching creature completes without moving a card")
    void noMatchingCreatureCompletesSearch() {
        castWildPair();
        Card nonmatchingCreature = new HillGiant();
        harness.setLibrary(player1, List.of(nonmatchingCreature));
        castGrizzlyBears();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatchingCreature);
        assertThat(countPermanents(player1, "Hill Giant")).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castWildPair() {
        harness.setHand(player1, List.of(new WildPair()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
    }

    private void castGrizzlyBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
    }

}
