package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.cards.d.DroolingOgre;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HoverguardObserver.class, DarksteelGargoyle.class, DroolingOgre.class})
class HoverguardObserverTest extends BaseCardTest {

    @Test
    @DisplayName("Hoverguard Observer can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent observer = addCreatureReady(player2, new HoverguardObserver());
        Permanent attacker = addCreatureReady(player1, new DarksteelGargoyle());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(observer.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Hoverguard Observer cannot block a creature without flying")
    void cannotBlockNonFlyingCreature() {
        addCreatureReady(player2, new HoverguardObserver());
        Permanent attacker = addCreatureReady(player1, new DroolingOgre());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }
}
