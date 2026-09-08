package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RevivalRevengeTest extends BaseCardTest {

    private static final int REVIVAL = 0;
    private static final int REVENGE = 1;

    @Test
    @DisplayName("Revival returns a targeted creature card with mana value 3 or less")
    void revivalReturnsCheapCreature() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new RevivalRevenge()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, REVIVAL, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Revival does not target a creature card with mana value greater than 3")
    void revivalRejectsExpensiveCreature() {
        Card hillGiant = new HillGiant();
        harness.setGraveyard(player1, List.of(hillGiant));
        harness.setHand(player1, List.of(new RevivalRevenge()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, REVIVAL, hillGiant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Revenge doubles your life and makes a target opponent lose half theirs")
    void revengeChangesBothLifeTotals() {
        harness.setLife(player1, 11);
        harness.setLife(player2, 19);
        harness.setHand(player1, List.of(new RevivalRevenge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, REVENGE, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(9);
    }

    @Test
    @DisplayName("Revenge cannot target its caster")
    void revengeRequiresOpponentTarget() {
        harness.setHand(player1, List.of(new RevivalRevenge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID playerId = player1.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, REVENGE, playerId))
                .isInstanceOf(IllegalStateException.class);
    }
}
