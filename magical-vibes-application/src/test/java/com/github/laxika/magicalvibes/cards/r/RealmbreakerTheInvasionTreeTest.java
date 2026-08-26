package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.EbonPraetor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RealmbreakerTheInvasionTree.class, Forest.class, Island.class, Plains.class,
        GrizzlyBears.class, EbonPraetor.class})
class RealmbreakerTheInvasionTreeTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability mills before the controller chooses a land from the opponent's graveyard")
    void millsThenControllerChoosesLand() {
        Permanent realmbreaker = addReadyRealmbreaker();
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
        assertThat(realmbreaker).isNotNull();
    }

    @Test
    @DisplayName("The second ability searches for any number of Praetors and lets the controller stop")
    void searchesForAnyNumberOfPraetors() {
        addReadyRealmbreaker();
        EbonPraetor firstPraetor = new EbonPraetor();
        EbonPraetor secondPraetor = new EbonPraetor();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(firstPraetor, secondPraetor, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(firstPraetor, secondPraetor);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

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

    private Permanent addReadyRealmbreaker() {
        harness.addToBattlefield(player1, new RealmbreakerTheInvasionTree());
        Permanent realmbreaker = gd.playerBattlefields.get(player1.getId()).getFirst();
        realmbreaker.setSummoningSick(false);
        return realmbreaker;
    }
}
