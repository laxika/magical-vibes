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
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

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

    private void addManaForX(int xValue) {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
    }
}
