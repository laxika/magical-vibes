package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Erode.class, Forest.class, GarrukWildspeaker.class, GrizzlyBears.class,
        Island.class, Mountain.class, Plains.class})
class ErodeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature and prompts its controller to search for a basic land (tapped)")
    void destroysCreatureAndPresentsTappedSearch() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        setupLibrary(player2);

        harness.setHand(player1, List.of(new Erode()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        acceptSearchIfOffered(player2);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Destroyed creature's controller puts the chosen basic land onto the battlefield tapped")
    void chosenLandEntersTapped() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        setupLibrary(player2);

        harness.setHand(player1, List.of(new Erode()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        acceptSearchIfOffered(player2);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND)
                        && p.getCard().getSupertypes().contains(CardSupertype.BASIC)
                        && p.isTapped());
    }

    @Test
    @DisplayName("Destroys target planeswalker")
    void destroysTargetPlaneswalker() {
        Permanent planeswalker = addReadyPlaneswalker(player2, 3);
        setupLibrary(player2);

        harness.setHand(player1, List.of(new Erode()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, planeswalker.getId());
        harness.passBothPriorities();
        acceptSearchIfOffered(player2);

        harness.assertNotOnBattlefield(player2, "Garruk Wildspeaker");
        harness.assertInGraveyard(player2, "Garruk Wildspeaker");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Erode()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID landId = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The target's controller can decline to search without changing their library")
    void controllerCanDeclineSearch() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        setupLibrary(player2);
        var originalLibrary = List.copyOf(gd.playerDecks.get(player2.getId()));
        harness.setHand(player1, List.of(new Erode()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(originalLibrary);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can destroy your own creature and search your own library")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        setupLibrary(player1);
        harness.setHand(player1, List.of(new Erode()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Plains") && p.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An illegal target prevents the search as well as destruction")
    void illegalTargetPreventsSearch() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        setupLibrary(player2);
        var originalLibrary = List.copyOf(gd.playerDecks.get(player2.getId()));
        harness.setHand(player1, List.of(new Erode()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(targetId));

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(originalLibrary);
        harness.assertInGraveyard(player1, "Erode");
    }

    private void acceptSearchIfOffered(Player player) {
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player, true);
        }
    }

    private void setupLibrary(Player player) {
        harness.setLibrary(player, List.of(new Plains(), new Island(), new Mountain(), new GrizzlyBears()));
    }

    private Permanent addReadyPlaneswalker(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GarrukWildspeaker());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        return perm;
    }
}
