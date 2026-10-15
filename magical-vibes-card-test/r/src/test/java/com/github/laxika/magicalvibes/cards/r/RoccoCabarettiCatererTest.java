package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CivilServant;
import com.github.laxika.magicalvibes.cards.c.ChromeCat;
import com.github.laxika.magicalvibes.cards.g.GatherSpecimens;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoccoCabarettiCaterer.class, CivilServant.class, ChromeCat.class, Plains.class, GatherSpecimens.class})
class RoccoCabarettiCatererTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, Rocco offers to search for a creature with mana value at most X")
    void castTriggersBoundedCreatureSearch() {
        Card eligible = new CivilServant();
        Card tooExpensive = new ChromeCat();
        harness.setLibrary(player1, List.of(eligible, tooExpensive, new Plains()));

        castRocco(2);
        resolveRoccoEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(eligible);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == eligible);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(eligible);
    }

    @Test
    @DisplayName("Declining the search leaves the library and battlefield unchanged")
    void decliningSearchDoesNothing() {
        Card eligible = new CivilServant();
        harness.setLibrary(player1, List.of(eligible));

        castRocco(2);
        resolveRoccoEtb();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eligible);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == eligible);
    }

    @Test
    @DisplayName("Putting Rocco onto the battlefield without casting it does not trigger the search")
    void enteringWithoutBeingCastDoesNothing() {
        Card eligible = new CivilServant();
        harness.setLibrary(player1, List.of(eligible));

        harness.enterBattlefieldAndReturn(player1, new RoccoCabarettiCaterer());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eligible);
    }

    @Test
    @DisplayName("X zero can search but cannot find a creature with positive mana value")
    void zeroXFindsNoEligibleCreature() {
        Card creature = new CivilServant();
        Card land = new Plains();
        harness.setLibrary(player1, List.of(creature, land));

        castRocco(0);
        resolveRoccoEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, land);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == creature);
    }

    @Test
    @DisplayName("A restricted search may fail to find even when an eligible creature exists")
    void canFailToFindEligibleCreature() {
        Card eligible = new CivilServant();
        harness.setLibrary(player1, List.of(eligible));

        castRocco(2);
        resolveRoccoEtb();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eligible);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == eligible);
    }

    @Test
    @DisplayName("The search can find a creature with mana value less than X")
    void canFindCreatureBelowX() {
        Card eligible = new CivilServant();
        harness.setLibrary(player1, List.of(eligible));

        castRocco(3);
        resolveRoccoEtb();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == eligible && !permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Rocco entering under a player who did not cast it does not trigger")
    void enteringUnderAnotherPlayersControlDoesNotTrigger() {
        harness.setLibrary(player2, List.of(new CivilServant()));
        castRocco(2);
        harness.castFromHand(player2, new GatherSpecimens(), "{3}{U}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Rocco, Cabaretti Caterer");
        harness.assertNotOnBattlefield(player1, "Rocco, Cabaretti Caterer");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castRocco(int xValue) {
        harness.setHand(player1, List.of(new RoccoCabarettiCaterer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        gs.playCard(gd, player1, 0, xValue, null, null);
    }

    private void resolveRoccoEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
