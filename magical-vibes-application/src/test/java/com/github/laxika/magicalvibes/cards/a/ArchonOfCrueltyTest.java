package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GeyadroneDihada;
import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArchonOfCruelty.class, GrizzlyBears.class, GeyadroneDihada.class, OrnithopterOfParadise.class})
class ArchonOfCrueltyTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes an opponent sacrifice, discard, and lose life while its controller draws and gains life")
    void entersAndResolvesCruelty() {
        harness.setHand(player1, List.of(new ArchonOfCruelty()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        addArchonMana();

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Attacking resolves the same ability")
    void attackingResolvesCruelty() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCreatureReady(player1, new ArchonOfCruelty());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player2, 11);
        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The rest of the ability resolves when the opponent controls no creature or planeswalker")
    void continuesWithoutSacrificeTarget() {
        harness.setHand(player1, List.of(new ArchonOfCruelty()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addArchonMana();

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 23);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The triggered ability cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new ArchonOfCruelty()));
        addArchonMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("The opponent can choose a planeswalker instead of a creature to sacrifice")
    void opponentChoosesPlaneswalker() {
        harness.setHand(player1, List.of(new ArchonOfCruelty()));
        harness.setHand(player2, List.of(new OrnithopterOfParadise()));
        harness.setLibrary(player1, List.of(new OrnithopterOfParadise()));
        harness.addToBattlefield(player2, new OrnithopterOfParadise());
        harness.addToBattlefield(player2, new GeyadroneDihada());
        addArchonMana();

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
        harness.handleMultiplePermanentsChosen(player2, List.of(findPermanent(player2, "Geyadrone Dihada").getId()));

        harness.assertInGraveyard(player2, "Geyadrone Dihada");
        harness.assertOnBattlefield(player2, "Ornithopter of Paradise");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Life loss, draw, and life gain still happen with no opposing hand or sacrifice candidates")
    void continuesWithEmptyOpponentHandAndBattlefield() {
        harness.setHand(player1, List.of(new ArchonOfCruelty()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new OrnithopterOfParadise()));
        addArchonMana();

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addArchonMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}
