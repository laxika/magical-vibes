package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArmaggonFutureShark.class, GrizzlyBears.class, Island.class})
class ArmaggonFutureSharkTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys up to three target creatures when it enters")
    void destroysThreeTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castArmaggon(List.of(first.getId(), second.getId(), third.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card instanceof GrizzlyBears)
                .hasSize(3);
    }

    @Test
    @DisplayName("Can enter without choosing any creatures")
    void canChooseNoCreatures() {
        castArmaggon(List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof ArmaggonFutureShark);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new ArmaggonFutureShark()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castArmaggon(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new ArmaggonFutureShark()));
        addMana();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}
