package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FootlightFiendTest extends BaseCardTest {

    @Test
    @DisplayName("When Footlight Fiend dies, it deals 1 damage to a target player")
    void deathTriggerDamagesTargetPlayer() {
        harness.addToBattlefield(player1, new FootlightFiend());
        int lifeBefore = gd.getLife(player2.getId());

        killFootlightFiend();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("When Footlight Fiend dies, it deals 1 damage to a target creature")
    void deathTriggerDamagesTargetCreature() {
        harness.addToBattlefield(player1, new FootlightFiend());
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");

        killFootlightFiend();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    private void killFootlightFiend() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(player1, "Footlight Fiend");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Footlight Fiend");
    }
}
