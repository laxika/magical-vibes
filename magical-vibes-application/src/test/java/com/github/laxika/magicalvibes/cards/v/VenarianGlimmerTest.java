package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.Calciderm;
import com.github.laxika.magicalvibes.cards.d.DawnCharm;
import com.github.laxika.magicalvibes.cards.m.ManaTithe;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Calciderm.class, DawnCharm.class, ManaTithe.class, UrborgTombOfYawgmoth.class,
        VenarianGlimmer.class})
class VenarianGlimmerTest extends BaseCardTest {

    @Test
    @DisplayName("Chooses a nonland card with mana value at most X and makes its player discard it")
    void choosesMatchingCardAndDiscardsIt() {
        UrborgTombOfYawgmoth land = new UrborgTombOfYawgmoth();
        ManaTithe manaTithe = new ManaTithe();
        DawnCharm dawnCharm = new DawnCharm();
        Calciderm calciderm = new Calciderm();
        harness.setHand(player1, List.of(new VenarianGlimmer()));
        harness.setHand(player2, List.of(land, manaTithe, dawnCharm, calciderm));
        addManaForX(2);

        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gameLogContains(player2.getUsername() + " reveals their hand")).isTrue();
        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(1, 2);

        harness.handleCardChosen(player1, 2);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(dawnCharm);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, manaTithe, calciderm);
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetController() {
        ManaTithe manaTithe = new ManaTithe();
        harness.setHand(player1, List.of(new VenarianGlimmer(), manaTithe));
        addManaForX(1);

        harness.castInstant(player1, 0, 1, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(manaTithe);
    }

    @Test
    @DisplayName("With X equal to zero, no positive-mana nonland card can be chosen")
    void noEligibleCardAtZero() {
        ManaTithe manaTithe = new ManaTithe();
        UrborgTombOfYawgmoth land = new UrborgTombOfYawgmoth();
        harness.setHand(player1, List.of(new VenarianGlimmer()));
        harness.setHand(player2, List.of(manaTithe, land));
        addManaForX(0);

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(manaTithe, land);
    }

    @Test
    @DisplayName("An empty hand resolves without a choice or discard")
    void emptyHandResolvesWithoutChoice() {
        harness.setHand(player1, List.of(new VenarianGlimmer()));
        harness.setHand(player2, List.of());
        addManaForX(2);

        harness.castInstant(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gameLogContains(player2.getUsername() + " reveals their hand. It is empty.")).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Venarian Glimmer");
    }

    @Test
    @DisplayName("The caster must choose an eligible card and the target cannot choose instead")
    void requiresEligibleChoiceByCaster() {
        ManaTithe manaTithe = new ManaTithe();
        UrborgTombOfYawgmoth land = new UrborgTombOfYawgmoth();
        DawnCharm dawnCharm = new DawnCharm();
        harness.setHand(player1, List.of(new VenarianGlimmer()));
        harness.setHand(player2, List.of(manaTithe, land, dawnCharm));
        addManaForX(1);

        harness.castInstant(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 2))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(manaTithe, land, dawnCharm);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(manaTithe);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, dawnCharm);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("X in a revealed hand card's mana cost contributes zero to its mana value")
    void xCostCardInHandUsesZeroForItsOwnX() {
        VenarianGlimmer handCard = new VenarianGlimmer();
        harness.setHand(player1, List.of(new VenarianGlimmer()));
        harness.setHand(player2, List.of(handCard));
        addManaForX(1);

        harness.castInstant(player1, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(handCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    private void addManaForX(int xValue) {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
    }
}
