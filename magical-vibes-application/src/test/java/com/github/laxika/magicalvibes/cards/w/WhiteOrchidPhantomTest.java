package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({WhiteOrchidPhantom.class, GhostQuarter.class, Forest.class})
class WhiteOrchidPhantomTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys a nonbasic land and its controller may fetch a tapped basic land")
    void destroysNonbasicLandAndFetchesTappedBasicLand() {
        Permanent basicLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player2, new GhostQuarter());
        harness.setLibrary(player2, List.of(new Forest()));

        castWhiteOrchidPhantom();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(nonbasicLand.getId()).doesNotContain(basicLand.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, basicLand.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, nonbasicLand.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ghost Quarter");
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Forest && permanent.isTapped());
    }

    @Test
    @DisplayName("ETB can resolve without choosing a target")
    void canResolveWithoutTarget() {
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player2, new GhostQuarter());

        castWhiteOrchidPhantom();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nonbasicLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The land's controller can decline searching without shuffling")
    void landControllerCanDeclineSearch() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new GhostQuarter());
        Forest first = new Forest();
        GhostQuarter second = new GhostQuarter();
        harness.setLibrary(player2, List.of(first, second));

        castWhiteOrchidPhantom();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ghost Quarter");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(first, second);
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("An accepted search offers only basic lands and may fail to find")
    void acceptedSearchCanFailToFind() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new GhostQuarter());
        Forest basic = new Forest();
        GhostQuarter nonbasic = new GhostQuarter();
        WhiteOrchidPhantom creature = new WhiteOrchidPhantom();
        harness.setLibrary(player2, List.of(basic, nonbasic, creature));

        castWhiteOrchidPhantom();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(basic);
        harness.handleCardChosen(player2, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrder(basic, nonbasic, creature);
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Ghost Quarter");
    }

    @Test
    @DisplayName("A controller can replace their own nonbasic land")
    void canTargetOwnNonbasicLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new GhostQuarter());
        harness.setLibrary(player1, List.of(new Forest()));

        castWhiteOrchidPhantom();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Ghost Quarter");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Forest && permanent.isTapped());
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("An indestructible target still lets its controller search")
    void indestructibleLandStillAllowsSearch() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new GhostQuarter());
        land.getPersistentGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setLibrary(player2, List.of(new Forest()));

        castWhiteOrchidPhantom();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Forest && permanent.isTapped());
    }

    @Test
    @DisplayName("A target that leaves before resolution prevents the search")
    void removedTargetDoesNotAllowSearch() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new GhostQuarter());
        Forest basic = new Forest();
        harness.setLibrary(player2, List.of(basic));

        castWhiteOrchidPhantom();
        harness.handlePermanentChosen(player1, land.getId());
        gd.playerBattlefields.get(player2.getId()).remove(land);
        gd.playerGraveyards.get(player2.getId()).add(land.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(basic);
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("No nonbasic lands are required for the creature to enter")
    void entersWithOnlyBasicLandsAvailable() {
        Permanent basic = harness.addToBattlefieldAndReturn(player2, new Forest());

        castWhiteOrchidPhantom();

        harness.assertOnBattlefield(player1, "White Orchid Phantom");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(basic);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castWhiteOrchidPhantom() {
        harness.setHand(player1, List.of(new WhiteOrchidPhantom()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
