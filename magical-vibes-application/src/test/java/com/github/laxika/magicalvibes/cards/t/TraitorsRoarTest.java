package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WallOfVines;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TraitorsRoarTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    @Test
    @DisplayName("Taps the target and it deals damage equal to its power to its controller")
    void tapsAndDealsPowerDamage() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new TraitorsRoar()));
        addMana();

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.castAndResolveSorcery(player1, 0, List.of(targetId));

        // Hill Giant is 3/3 -> its controller loses 3, and the creature ends up tapped and unharmed.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getId().equals(targetId))
                .allMatch(Permanent::isTapped)
                .allMatch(p -> p.getMarkedDamage() == 0);
    }

    @Test
    @DisplayName("A 0-power creature is tapped but deals no damage")
    void zeroPowerDealsNoDamage() {
        harness.addToBattlefield(player2, new WallOfVines());
        harness.setHand(player1, List.of(new TraitorsRoar()));
        addMana();

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        UUID targetId = harness.getPermanentId(player2, "Wall of Vines");
        harness.castAndResolveSorcery(player1, 0, List.of(targetId));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getId().equals(targetId))
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Cannot target an already-tapped creature")
    void cannotTargetTappedCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID tappedId = harness.getPermanentId(player1, "Grizzly Bears");
        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(tappedId)).findFirst().orElseThrow().tap();
        harness.setHand(player1, List.of(new TraitorsRoar()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(tappedId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
