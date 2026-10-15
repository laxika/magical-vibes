package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.f.FrenzySliver;
import com.github.laxika.magicalvibes.cards.o.Ovinize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HomingSliver.class, FrenzySliver.class, BlindPhantasm.class, Ovinize.class})
class HomingSliverTest extends BaseCardTest {

    @Test
    @DisplayName("A Homing Sliver that loses all abilities stops granting Slivercycling")
    void abilityLossStopsGrantingSlivercycling() {
        var homing = harness.addToBattlefieldAndReturn(player1, new HomingSliver());
        harness.setHand(player1, List.of(new Ovinize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, homing.getId());
        harness.setHand(player1, List.of(new FrenzySliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card has no hand-activated ability");
        harness.assertInHand(player1, "Frenzy Sliver");
        harness.assertNotInGraveyard(player1, "Frenzy Sliver");
    }

    @Test
    @DisplayName("Homing Sliver in hand does not grant Slivercycling to other cards")
    void doesNotGrantFromHand() {
        harness.setHand(player1, List.of(new FrenzySliver(), new HomingSliver()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card has no hand-activated ability");
    }

    @Test
    @DisplayName("Slivercycling cannot be activated without three mana and does not discard on failure")
    void insufficientManaDoesNotDiscard() {
        HomingSliver card = new HomingSliver();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        harness.assertNotInGraveyard(player1, "Homing Sliver");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Slivercycling permits failing to find even when a Sliver is available")
    void mayFailToFind() {
        harness.setHand(player1, List.of(new HomingSliver()));
        FrenzySliver sliver = new FrenzySliver();
        harness.setLibrary(player1, List.of(sliver));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Homing Sliver");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sliver);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Homing Sliver grants Slivercycling to Sliver cards in each player's hand")
    void grantsSlivercyclingToOpposingSliverCard() {
        harness.addToBattlefield(player1, new HomingSliver());
        harness.setHand(player2, List.of(new FrenzySliver()));
        harness.setLibrary(player2, List.of(new FrenzySliver(), new BlindPhantasm()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(card -> card.getName())
                .containsExactly("Frenzy Sliver");

        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Frenzy Sliver");
        harness.assertInHand(player2, "Frenzy Sliver");
    }

    @Test
    @DisplayName("Homing Sliver grants Slivercycling to Sliver cards in its controller's hand")
    void grantsSlivercyclingToControllersSliverCard() {
        harness.addToBattlefield(player1, new HomingSliver());
        harness.setHand(player1, List.of(new FrenzySliver()));
        harness.setLibrary(player1, List.of(new FrenzySliver(), new BlindPhantasm()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(card -> card.getName())
                .containsExactly("Frenzy Sliver");

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Frenzy Sliver");
        harness.assertInHand(player1, "Frenzy Sliver");
    }

    @Test
    @DisplayName("Homing Sliver does not grant Slivercycling to non-Sliver cards")
    void doesNotGrantSlivercyclingToNonSliverCard() {
        harness.addToBattlefield(player1, new HomingSliver());
        harness.setHand(player1, List.of(new BlindPhantasm()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card has no hand-activated ability");
    }

    @Test
    @DisplayName("Homing Sliver has its own Slivercycling while in hand")
    void hasPrintedSlivercyclingInHand() {
        harness.setHand(player1, List.of(new HomingSliver()));
        FrenzySliver sliver = new FrenzySliver();
        harness.setLibrary(player1, List.of(new BlindPhantasm(), sliver));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(sliver);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sliver);
        harness.assertInGraveyard(player1, "Homing Sliver");
    }
}
