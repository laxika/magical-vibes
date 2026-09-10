package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShuGeneral.class, ShuFootSoldiers.class, ShuCavalry.class})
class ShuGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("Vigilance keeps Shu General untapped after attacking")
    void vigilanceKeepsItUntappedWhenItAttacks() {
        Permanent shuGeneral = addCreatureReady(player1, new ShuGeneral());

        declareAttackers(List.of(0));

        assertThat(shuGeneral.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Shu General can't be blocked by a creature without horsemanship")
    void cannotBeBlockedByCreatureWithoutHorsemanship() {
        addCreatureReady(player2, new ShuFootSoldiers());
        Permanent attacker = addCreatureReady(player1, new ShuGeneral());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("horsemanship");
    }

    @Test
    @DisplayName("Shu General can be blocked by a creature with horsemanship")
    void canBeBlockedByCreatureWithHorsemanship() {
        Permanent blocker = addCreatureReady(player2, new ShuCavalry());
        Permanent attacker = addCreatureReady(player1, new ShuGeneral());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
