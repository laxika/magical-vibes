package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FearOfLostTeeth.class, LlanowarElves.class, Shock.class})
class FearOfLostTeethTest extends BaseCardTest {

    @Test
    @DisplayName("When Fear of Lost Teeth dies, it damages a target player and its controller gains 1 life")
    void deathTriggerDamagesPlayerAndGainsLife() {
        harness.addToBattlefield(player1, new FearOfLostTeeth());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killFearOfLostTeeth();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("When Fear of Lost Teeth dies, it can damage a target creature and its controller gains 1 life")
    void deathTriggerDamagesCreatureAndGainsLife() {
        harness.addToBattlefield(player1, new FearOfLostTeeth());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        killFearOfLostTeeth();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(elvesId, player2.getId());
        harness.handlePermanentChosen(player1, elvesId);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    private void killFearOfLostTeeth() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID fearId = harness.getPermanentId(player1, "Fear of Lost Teeth");
        harness.castInstant(player1, 0, fearId);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Fear of Lost Teeth");
    }
}
