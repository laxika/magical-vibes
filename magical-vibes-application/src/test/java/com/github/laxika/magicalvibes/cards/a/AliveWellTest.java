package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AliveWellTest extends BaseCardTest {

    private static final int ALIVE = 0;
    private static final int WELL = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Alive creates a 3/3 green Centaur token")
    void aliveCreatesCentaur() {
        harness.setHand(player1, List.of(new AliveWell()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, ALIVE);
        harness.passBothPriorities();

        List<Permanent> centaurs = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.CENTAUR))
                .toList();
        assertThat(centaurs).hasSize(1);
        assertThat(centaurs.getFirst().getCard().isToken()).isTrue();
        assertThat(centaurs.getFirst().getEffectivePower()).isEqualTo(3);
        assertThat(centaurs.getFirst().getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Well gains 2 life for each creature you control")
    void wellGainsLifeForControlledCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AliveWell()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, WELL);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    @DisplayName("Fuse creates the Centaur before counting creatures for Well")
    void fuseResolvesAliveBeforeWell() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AliveWell()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castModalSorcery(player1, 0, FUSE, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anySatisfy(permanent ->
                assertThat(permanent.getCard().getSubtypes()).contains(CardSubtype.CENTAUR));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 6);
    }

    @Test
    @DisplayName("Fuse requires the combined cost")
    void fuseRequiresBothHalvesCost() {
        harness.setHand(player1, List.of(new AliveWell()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, FUSE, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
