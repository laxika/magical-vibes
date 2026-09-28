package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DromadPurebred;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Mortipede.class, DromadPurebred.class})
class MortipedeTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability requires all able creatures to block Mortipede")
    void allAbleCreaturesMustBlock() {
        addCreatureReady(player1, new Mortipede());
        addCreatureReady(player2, new DromadPurebred());
        addCreatureReady(player2, new DromadPurebred());
        activateMortipedeAbility();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
    }

    @Test
    @DisplayName("Tapped creatures are not required to block Mortipede")
    void tappedCreaturesAreNotRequiredToBlock() {
        addCreatureReady(player1, new Mortipede());
        Permanent untapped = addCreatureReady(player2, new DromadPurebred());
        Permanent tapped = addCreatureReady(player2, new DromadPurebred());
        tapped.tap();
        activateMortipedeAbility();

        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(untapped.isBlocking()).isTrue();
        assertThat(tapped.isBlocking()).isFalse();
    }

    private void activateMortipedeAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
