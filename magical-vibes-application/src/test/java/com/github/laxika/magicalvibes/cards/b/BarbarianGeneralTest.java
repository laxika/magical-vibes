package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.ShuCavalry;
import com.github.laxika.magicalvibes.cards.s.ShuFootSoldiers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BarbarianGeneral.class, ShuCavalry.class, ShuFootSoldiers.class})
class BarbarianGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("Barbarian General can't be blocked by a creature without horsemanship")
    void cannotBeBlockedByCreatureWithoutHorsemanship() {
        Permanent blocker = addCreatureReady(player2, new ShuFootSoldiers());
        addCreatureReady(player1, new BarbarianGeneral());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("horsemanship");
    }

    @Test
    @DisplayName("Barbarian General can be blocked by a creature with horsemanship")
    void canBeBlockedByCreatureWithHorsemanship() {
        Permanent blocker = addCreatureReady(player2, new ShuCavalry());
        addCreatureReady(player1, new BarbarianGeneral());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
