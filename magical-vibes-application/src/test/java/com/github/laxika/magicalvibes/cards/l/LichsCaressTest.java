package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TotallyLost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LichsCaress.class, GrizzlyBears.class, Plains.class, DarksteelMyr.class, TotallyLost.class})
class LichsCaressTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature and gains 3 life")
    void destroysCreatureAndGainsLife() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castLichsCaress();

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, List.of(targetId));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can destroy your own creature")
    void destroysOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castLichsCaress();

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, List.of(targetId));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 23);
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

    @Test
    @DisplayName("Gains life even when an indestructible creature cannot be destroyed")
    void gainsLifeWhenDestructionFails() {
        harness.addToBattlefield(player2, new DarksteelMyr());
        castLichsCaress();

        UUID targetId = harness.getPermanentId(player2, "Darksteel Myr");
        harness.castAndResolveSorcery(player1, 0, List.of(targetId));

        harness.assertOnBattlefield(player2, "Darksteel Myr");
        harness.assertInGraveyard(player1, "Lich's Caress");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not gain life when the only target leaves before resolution")
    void doesNotGainLifeWhenTargetLeaves() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castLichsCaress();
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, List.of(targetId));

        harness.setHand(player2, List.of(new TotallyLost()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lich's Caress");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Rejects player targets")
    void rejectsPlayerTarget() {
        castLichsCaress();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castLichsCaress() {
        harness.setHand(player1, List.of(new LichsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 5);
    }
}
