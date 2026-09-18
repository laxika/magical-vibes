package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EternalDragon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Dragonstalker.class, DaruWarchief.class, EternalDragon.class})
class DragonstalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Dragonstalker has protection from Dragons")
    void hasProtectionFromDragons() {
        Permanent dragonstalker = addCreatureReady(player1, new Dragonstalker());
        Permanent dragon = addCreatureReady(player2, new EternalDragon());
        Permanent nonDragon = addCreatureReady(player2, new DaruWarchief());

        assertThat(gqs.hasProtectionFromSource(gd, dragonstalker, dragon)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, dragonstalker, nonDragon)).isFalse();
    }

    @Test
    @DisplayName("Dragon creatures cannot block Dragonstalker")
    void dragonCreatureCannotBlock() {
        addCreatureReady(player1, new Dragonstalker());
        addCreatureReady(player2, new EternalDragon());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Combat damage from Dragon creatures is prevented")
    void combatDamageFromDragonIsPrevented() {
        Permanent dragon = addCreatureReady(player1, new EternalDragon());
        Permanent dragonstalker = addCreatureReady(player2, new Dragonstalker());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(dragonstalker.getMarkedDamage()).isZero();
        assertThat(dragon.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dragon);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(dragonstalker);
    }
}
