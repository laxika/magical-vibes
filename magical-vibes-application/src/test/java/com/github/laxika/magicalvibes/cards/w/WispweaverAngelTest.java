package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WispweaverAngelTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may flicker another creature you control")
    void etbMayFlickerAnotherCreatureYouControl() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        UUID oldBearsId = bears.getId();

        castAngel(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, oldBearsId);

        Permanent returnedBears = findPermanent(player1, "Grizzly Bears");
        assertThat(returnedBears.getId()).isNotEqualTo(oldBearsId);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the ETB ability leaves the target on the battlefield")
    void decliningEtbLeavesTargetOnBattlefield() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        castAngel(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Grizzly Bears").getId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WispweaverAngel()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAngel(Player player, UUID targetId) {
        harness.setHand(player, List.of(new WispweaverAngel()));
        harness.addMana(player, ManaColor.WHITE, 6);
        harness.castCreature(player, 0, 0, targetId);
    }
}
