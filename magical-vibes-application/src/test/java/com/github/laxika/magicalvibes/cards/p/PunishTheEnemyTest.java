package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PunishTheEnemyTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 to the targeted player and 3 to the targeted creature")
    void damagesPlayerAndCreature() {
        Permanent serra = harness.addToBattlefieldAndReturn(player2, new SerraAngel()); // 4/4
        UUID serraId = serra.getId();
        harness.setHand(player1, List.of(new PunishTheEnemy()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, List.of(player2.getId(), serraId));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(serra.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target its own controller")
    void canTargetController() {
        Permanent serra = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        harness.setHand(player1, List.of(new PunishTheEnemy()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, List.of(player1.getId(), serra.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(serra.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Rejects a player as the creature target")
    void rejectsPlayerAsCreatureTarget() {
        harness.setHand(player1, List.of(new PunishTheEnemy()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a creature as the player-or-planeswalker target")
    void rejectsCreatureAsPlayerTarget() {
        Permanent serra = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new PunishTheEnemy()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(serra.getId(), serra.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
