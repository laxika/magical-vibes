package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoliathBeetle;
import com.github.laxika.magicalvibes.cards.h.HulkingOgre;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TauntingElf.class, GoliathBeetle.class, HulkingOgre.class})
class TauntingElfTest extends BaseCardTest {

    @Test
    @DisplayName("All able creatures must block Taunting Elf")
    void allAbleCreaturesMustBlock() {
        Permanent elf = addCreatureReady(player1, new TauntingElf());
        elf.setAttacking(true);

        addCreatureReady(player2, new GoliathBeetle());
        addCreatureReady(player2, new GoliathBeetle());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tapped creatures are not forced to block Taunting Elf")
    void tappedCreaturesAreNotForcedToBlock() {
        Permanent elf = addCreatureReady(player1, new TauntingElf());
        elf.setAttacking(true);

        Permanent untapped = addCreatureReady(player2, new GoliathBeetle());
        Permanent tapped = addCreatureReady(player2, new GoliathBeetle());
        tapped.tap();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(untapped.isBlocking()).isTrue();
        assertThat(tapped.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Creatures that cannot block are not forced to block Taunting Elf")
    void creaturesThatCannotBlockAreNotForcedToBlock() {
        Permanent elf = addCreatureReady(player1, new TauntingElf());
        elf.setAttacking(true);

        Permanent able = addCreatureReady(player2, new GoliathBeetle());
        Permanent unable = addCreatureReady(player2, new HulkingOgre());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(able.isBlocking()).isTrue();
        assertThat(unable.isBlocking()).isFalse();
    }
}
