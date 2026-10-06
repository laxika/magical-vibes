package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.e.EbonPraetor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RealmbreakerTheInvasionTree.class, Forest.class, Island.class, Plains.class,
        GrizzlyBears.class, EbonPraetor.class, DryadArbor.class, DressDown.class})
class RealmbreakerTheInvasionTreeTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability mills before the controller chooses a land from the opponent's graveyard")
    void millsThenControllerChoosesLand() {
        addReadyRealmbreaker();
        Card milledForest = new Forest();
        Card milledIsland = new Island();
        Card milledCreature = new GrizzlyBears();
        Card existingLand = new Plains();
        harness.setLibrary(player2, List.of(milledForest, milledCreature, milledIsland));
        harness.setGraveyard(player2, List.of(existingLand));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.cardPool()).containsExactlyInAnyOrder(existingLand, milledForest, milledIsland);

        harness.handleGraveyardCardChosen(player1, choice.cardPool().indexOf(milledForest));

        Permanent fetchedLand = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(milledForest.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(fetchedLand.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(existingLand, milledIsland, milledCreature)
                .doesNotContain(milledForest);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, fetchedLand));
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(milledForest);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(milledForest);
    }

    @Test
    @DisplayName("The second ability searches for any number of Praetors and lets the controller stop")
    void searchesForAnyNumberOfPraetors() {
        addReadyRealmbreaker();
        EbonPraetor firstPraetor = new EbonPraetor();
        EbonPraetor secondPraetor = new EbonPraetor();
        harness.setLibrary(player1, List.of(firstPraetor, secondPraetor, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(firstPraetor, secondPraetor);

        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)).contains(firstPraetor).doesNotContain(secondPraetor);
        assertThat(gd.playerDecks.get(player1.getId())).contains(secondPraetor)
                .anyMatch(card -> card instanceof GrizzlyBears);
        harness.assertInGraveyard(player1, "Realmbreaker, the Invasion Tree");
    }

    @Test
    @DisplayName("The first ability cannot target its controller")
    void cannotTargetController() {
        addReadyRealmbreaker();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void putsAllSelectedPraetorsOntoBattlefieldTogether() {
        addReadyRealmbreaker();
        EbonPraetor first = new EbonPraetor();
        EbonPraetor second = new EbonPraetor();
        Card other = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, other));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream().map(Permanent::getCard))
                .doesNotContain(first, second);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream().map(Permanent::getCard))
                .contains(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Realmbreaker, the Invasion Tree");
    }

    @Test
    void canChooseZeroPraetors() {
        addReadyRealmbreaker();
        Card praetor = new EbonPraetor();
        harness.setLibrary(player1, List.of(praetor));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(praetor);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Realmbreaker, the Invasion Tree");
    }

    @Test
    void searchWithEmptyLibraryStillSacrificesRealmbreaker() {
        addReadyRealmbreaker();
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Realmbreaker, the Invasion Tree");
        harness.assertInGraveyard(player1, "Realmbreaker, the Invasion Tree");
    }

    @Test
    void millsShortLibraryEvenWhenNoLandIsAvailable() {
        addReadyRealmbreaker();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(creature));
        harness.setGraveyard(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void takesExistingLandEvenWhenOpponentLibraryIsEmpty() {
        addReadyRealmbreaker();
        Card land = new Forest();
        harness.setLibrary(player2, List.of());
        harness.setGraveyard(player2, List.of(land));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        Permanent fetched = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(land.getId()))
                .findFirst().orElseThrow();
        assertThat(fetched.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({DryadArbor.class, DressDown.class})
    void stolenLandCanLoseItsGrantedExileAbility() {
        addReadyRealmbreaker();
        Card arbor = new DryadArbor();
        harness.setLibrary(player2, List.of());
        harness.setGraveyard(player2, List.of(arbor));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        Permanent fetched = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(arbor.getId()))
                .findFirst().orElseThrow();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new DressDown(), "{1}{U}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Dress Down");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, fetched));

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(arbor);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(arbor);
    }

    private Permanent addReadyRealmbreaker() {
        Permanent realmbreaker = harness.addToBattlefieldAndReturn(player1, new RealmbreakerTheInvasionTree());
        realmbreaker.setSummoningSick(false);
        return realmbreaker;
    }
}
