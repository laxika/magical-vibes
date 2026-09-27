package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LastGasp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CentaurSafeguard.class, LastGasp.class})
class CentaurSafeguardTest extends BaseCardTest {

    @Test
    @DisplayName("When Centaur Safeguard dies, its controller may gain 3 life")
    void acceptsDeathTriggerToGainLife() {
        harness.addToBattlefield(player1, new CentaurSafeguard());
        harness.setLife(player1, 10);
        destroyCentaurSafeguard();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Declining Centaur Safeguard's death trigger gains no life")
    void declinesDeathTrigger() {
        harness.addToBattlefield(player1, new CentaurSafeguard());
        harness.setLife(player1, 10);
        destroyCentaurSafeguard();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("The creature's controller gains the life, not the player who caused its death")
    void gainsLifeForControllerOfDyingCreature() {
        harness.addToBattlefield(player2, new CentaurSafeguard());
        harness.setLife(player2, 10);
        destroyCentaurSafeguard(player2, player1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    private void destroyCentaurSafeguard() {
        destroyCentaurSafeguard(player1, player2);
    }

    private void destroyCentaurSafeguard(Player creatureController, Player destroyer) {
        harness.setHand(destroyer, List.of(new LastGasp()));
        harness.addMana(destroyer, ManaColor.BLACK, 1);
        harness.addMana(destroyer, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(destroyer, 0,
                harness.getPermanentId(creatureController, "Centaur Safeguard"));
    }
}
