package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WarriorEnKor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IcewindStalwart.class, GrizzlyBears.class, WarriorEnKor.class})
class IcewindStalwartTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles and immediately returns a non-Warrior creature you control")
    void flickersTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID originalId = bears.getId();
        castStalwart(originalId);

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(originalId);
        assertThat(returned.isSummoningSick()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB can resolve without a target")
    void canChooseNoTarget() {
        harness.setHand(player1, List.of(new IcewindStalwart()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, (UUID) null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Icewind Stalwart");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a Warrior")
    void cannotTargetWarrior() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new WarriorEnKor());
        harness.setHand(player1, List.of(new IcewindStalwart()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, warrior.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Warrior creature you control");
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IcewindStalwart()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Warrior creature you control");
    }

    private void castStalwart(UUID targetId) {
        harness.setHand(player1, List.of(new IcewindStalwart()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
