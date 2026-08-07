package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlhammarretHighArbiterTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving reveals the opponent's hand and offers only its nonland card names")
    void resolvingRevealsHandAndOffersNonlandNames() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new Forest()));
        castAlhammarret();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("reveals their hand"));
        // The permanent may not enter before the name is chosen (CR 614.1c).
        harness.assertNotOnBattlefield(player1, "Alhammarret, High Arbiter");

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("A card only in the controller's own hand is not offered as a name")
    void ownHandCardsAreNotOffered() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new AlhammarretHighArbiter(), new HillGiant()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("The chosen name is recorded on the permanent once it enters")
    void chosenNameRecordedOnPermanent() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        castAlhammarret();
        harness.handleListChoice(player1, "Grizzly Bears");

        Permanent alhammarret = findPermanent(player1, "Alhammarret, High Arbiter");
        assertThat(alhammarret.getChosenName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("With no nonland card in the opponent's hand it enters without a name choice")
    void noCandidateNamesEntersImmediately() {
        harness.setHand(player2, List.of(new Forest()));
        castAlhammarret();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player1, "Alhammarret, High Arbiter").getChosenName()).isNull();
    }

    @Test
    @DisplayName("Opponents can't cast spells with the chosen name")
    void opponentCannotCastChosenName() {
        addReadyArbiter(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Opponents can still cast spells with a different name")
    void opponentCanCastOtherNames() {
        addReadyArbiter(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The controller can still cast spells with the chosen name")
    void controllerCanStillCastChosenName() {
        addReadyArbiter(player1, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    private void castAlhammarret() {
        harness.setHand(player1, List.of(new AlhammarretHighArbiter()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void addReadyArbiter(Player player, String chosenName) {
        Permanent perm = new Permanent(new AlhammarretHighArbiter());
        perm.setChosenName(chosenName);
        gd.playerBattlefields.get(player.getId()).add(perm);
    }
}
