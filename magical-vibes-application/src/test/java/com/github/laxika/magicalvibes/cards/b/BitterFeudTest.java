package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BitterFeud.class, Shock.class, SerraAngel.class})
class BitterFeudTest extends BaseCardTest {

    @Test
    @DisplayName("As it enters, Bitter Feud chooses two distinct players")
    void choosesTwoPlayersOnEntry() {
        Permanent feud = castBitterFeudSpell();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(feud.getChosenPlayerIds()).containsExactly(player1.getId(), player2.getId());
    }

    @Test
    @DisplayName("Doubles damage from either chosen player to the other chosen player")
    void doublesDamageBetweenChosenPlayers() {
        castBitterFeud();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Doubles damage to a permanent controlled by the chosen player")
    void doublesDamageToChosenPlayersPermanent() {
        castBitterFeud();
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0, angel.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Serra Angel");
    }

    private Permanent castBitterFeud() {
        Permanent feud = castBitterFeudSpell();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        return feud;
    }

    private Permanent castBitterFeudSpell() {
        harness.setHand(player1, List.of(new BitterFeud()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        Permanent feud = findPermanent(player1, "Bitter Feud");
        return feud;
    }
}
