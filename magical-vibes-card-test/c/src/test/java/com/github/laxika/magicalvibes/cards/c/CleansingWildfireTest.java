package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CleansingWildfire.class, Forest.class, Island.class, GrizzlyBears.class})
class CleansingWildfireTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target land, offers its controller a tapped basic, and draws a card")
    void destroysLandSearchesForTappedBasicAndDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setLibrary(player2, List.of(new Island()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castWildfire(target);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        harness.handleMayAbilityChosen(player2, true);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player2, 0);

        assertThat(findPermanent(player2, "Island").isTapped()).isTrue();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The destroyed land's controller may fail to find a basic and the caster still draws")
    void mayFailToFindAndCasterStillDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setLibrary(player2, List.of(new Island()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castWildfire(target);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, -1);

        harness.assertInGraveyard(player2, "Forest");
        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonland() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new CleansingWildfire()));
        addWildfireMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land");
    }

    @Test
    void mayDeclineSearchingWithoutShuffling() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        Island top = new Island();
        Forest bottom = new Forest();
        harness.setLibrary(player2, List.of(top, bottom));
        harness.setLibrary(player1, List.of(new Island()));
        castWildfire(target);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top, bottom);
        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInHand(player1, "Island");
    }

    @Test
    void survivingIndestructibleLandStillAllowsSearchAndDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        target.getPersistentGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setLibrary(player2, List.of(new Island()));
        harness.setLibrary(player1, List.of(new Island()));
        castWildfire(target);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
        assertThat(findPermanent(player2, "Island").isTapped()).isTrue();
        harness.assertInHand(player1, "Island");
    }

    @Test
    void canDestroyOwnLandAndSearchBeforeDrawing() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new Island(), new CleansingWildfire()));
        castWildfire(target);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(findPermanent(player1, "Island").isTapped()).isTrue();
        harness.assertInHand(player1, "Cleansing Wildfire");
    }

    @Test
    void noBasicInLibraryStillDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setLibrary(player2, List.of(new CleansingWildfire()));
        harness.setLibrary(player1, List.of(new Island()));
        castWildfire(target);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player2, "Forest");
        harness.assertInHand(player1, "Island");
    }

    @Test
    void removedTargetPreventsSearchAndDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setLibrary(player2, List.of(new Island()));
        Island draw = new Island();
        harness.setLibrary(player1, List.of(draw));
        castWildfire(target);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        harness.assertNotInHand(player1, "Island");
        harness.assertNotOnBattlefield(player2, "Island");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castWildfire(Permanent target) {
        harness.setHand(player1, List.of(new CleansingWildfire()));
        addWildfireMana();
        harness.castSorcery(player1, 0, target.getId());
    }

    private void addWildfireMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

}
