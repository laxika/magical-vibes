package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LichsCaressTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature and gains 3 life")
    void destroysCreatureAndGainsLife() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castLichsCaress();

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(targetId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can destroy your own creature")
    void destroysOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castLichsCaress();

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(targetId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Rejects non-creature targets")
    void rejectsLandTarget() {
        harness.addToBattlefield(player2, new Plains());
        castLichsCaress();

        UUID landId = harness.getPermanentId(player2, "Plains");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(landId)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castLichsCaress() {
        harness.setHand(player1, List.of(new LichsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 5);
    }
}
