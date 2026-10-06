package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sandworm.class, Forest.class, Plains.class})
class SandwormTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys the target land and lets its controller search for a tapped basic land")
    void etbDestroysLandAndSearchesForItsController() {
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player2, List.of(new Plains()));
        harness.setHand(player1, List.of(new Sandworm()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castCreature(player1, 0, 0, targetId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.decidingPlayerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.decidingPlayerId()).isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        Permanent plains = findPermanent(player2, "Plains");
        assertThat(plains.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player2, new Sandworm());
        harness.setHand(player1, List.of(new Sandworm()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Sandworm");
        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB does not search if the target land leaves before resolution")
    void etbDoesNotSearchIfTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player2, List.of(new Plains()));
        harness.setHand(player1, List.of(new Sandworm()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Sandworm");
    }

    @Test
    @DisplayName("The land controller can decline searching after the land is destroyed")
    void landControllerCanDeclineSearch() {
        harness.addToBattlefield(player2, new Forest());
        List<Forest> library = List.of(new Forest(), new Forest(), new Forest());
        harness.setLibrary(player2, library);
        harness.setHand(player1, List.of(new Sandworm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0, harness.getPermanentId(player2, "Forest"));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Forest");
        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.decidingPlayerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library);
        assertThat(gameLogContains("searches their library")).isFalse();
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Sandworm can destroy its controller's land and search that controller's library")
    void canTargetOwnLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new Sandworm(), new Plains()));
        harness.setHand(player1, List.of(new Sandworm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0, harness.getPermanentId(player1, "Forest"));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Forest");
        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.decidingPlayerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(search.params().cards()).hasSize(1);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Plains").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The land controller may search but fail to find an available basic land")
    void canFailToFindBasicLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player2, List.of(new Plains()));
        harness.setHand(player1, List.of(new Sandworm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0, harness.getPermanentId(player2, "Forest"));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, -1);

        harness.assertInGraveyard(player2, "Forest");
        harness.assertNotOnBattlefield(player2, "Plains");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sandworm can attack the turn it enters")
    void canAttackImmediately() {
        harness.setHand(player1, List.of(new Sandworm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(findPermanent(player1, "Sandworm").isAttacking()).isTrue();
    }
}
