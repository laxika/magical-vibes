package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThassasRebuffTest extends BaseCardTest {

    private void castRebuffOnBears(GrizzlyBears bears, int extraMana) {
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2 + extraMana);

        harness.setHand(player2, List.of(new ThassasRebuff()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
    }

    @Test
    @DisplayName("Counters the spell when its controller cannot pay blue devotion")
    void countersWhenControllerCannotPay() {
        harness.addToBattlefield(player2, new AirElemental()); // Two blue mana symbols -> pay {2}

        GrizzlyBears bears = new GrizzlyBears();
        castRebuffOnBears(bears, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Lets the spell resolve when its controller pays the blue devotion cost")
    void notCounteredWhenControllerPays() {
        harness.addToBattlefield(player2, new AirElemental()); // Two blue mana symbols -> pay {2}

        GrizzlyBears bears = new GrizzlyBears();
        castRebuffOnBears(bears, 2);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotNull();
    }
}
