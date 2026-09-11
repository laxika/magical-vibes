package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InspiringCleric;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({ThunderingSparkmage.class, BoggartBrute.class, ChandraNalaar.class,
        FaerieMiscreant.class, FugitiveWizard.class, GrizzlyBears.class,
        InspiringCleric.class, Island.class})
class ThunderingSparkmageTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the controller's party size")
    void dealsDamageEqualToPartySize() {
        harness.addToBattlefield(player1, new InspiringCleric());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new BoggartBrute());

        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        castSparkmage(planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts only party creatures controlled by the Sparkmage's controller")
    void countsOnlyControllersParty() {
        harness.addToBattlefield(player2, new InspiringCleric());
        harness.addToBattlefield(player2, new FaerieMiscreant());
        harness.addToBattlefield(player2, new BoggartBrute());
        harness.addToBattlefield(player2, new FugitiveWizard());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSparkmage(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ThunderingSparkmage()));
        addSparkmageMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or planeswalker");
    }

    private void castSparkmage(UUID targetId) {
        harness.setHand(player1, List.of(new ThunderingSparkmage()));
        addSparkmageMana();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addSparkmageMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
