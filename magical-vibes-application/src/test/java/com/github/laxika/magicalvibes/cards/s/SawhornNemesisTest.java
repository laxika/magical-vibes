package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SawhornNemesis.class, Shock.class, GrizzlyBears.class})
class SawhornNemesisTest extends BaseCardTest {

    @Test
    @DisplayName("As it enters, Sawhorn Nemesis chooses a player")
    void choosesPlayerOnEntry() {
        Permanent nemesis = castSawhornNemesis();

        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(nemesis.getRememberedTargetPlayerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Doubles damage dealt to the chosen player")
    void doublesDamageToChosenPlayer() {
        castSawhornNemesis();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Doubles damage dealt to a permanent controlled by the chosen player")
    void doublesDamageToChosenPlayersPermanent() {
        castSawhornNemesis();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not double damage dealt to another player")
    void doesNotDoubleDamageToAnotherPlayer() {
        castSawhornNemesis();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    private Permanent castSawhornNemesis() {
        harness.setHand(player1, List.of(new SawhornNemesis()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Sawhorn Nemesis");
    }
}
