package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CentaurSafeguard.class, Shock.class})
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

    private void destroyCentaurSafeguard() {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Centaur Safeguard"));
        harness.passBothPriorities();
    }
}
