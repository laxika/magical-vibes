package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BattlegroundGeist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EssenceFluxTest extends BaseCardTest {

    @Test
    @DisplayName("Flickers a non-Spirit creature without a +1/+1 counter")
    void flickerNonSpiritNoCounter() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EssenceFlux()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(bearsId);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Flickers a Spirit and returns it with a +1/+1 counter")
    void flickerSpiritGetsCounter() {
        harness.addToBattlefield(player1, new BattlegroundGeist());
        harness.setHand(player1, List.of(new EssenceFlux()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID geistId = harness.getPermanentId(player1, "Battleground Geist");
        harness.castInstant(player1, 0, geistId);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Battleground Geist"))
                .findFirst().orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(geistId);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EssenceFlux()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentBearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returned creature goes to its owner (not necessarily the controller)")
    void returnsUnderOwnersControl() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");
        gd.stolenCreatures.put(bearsPermId, player2.getId());

        harness.setHand(player1, List.of(new EssenceFlux()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, bearsPermId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }
}
