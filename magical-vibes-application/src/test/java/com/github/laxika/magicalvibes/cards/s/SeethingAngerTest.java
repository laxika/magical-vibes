package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeethingAngerTest extends BaseCardTest {

    @Test
    @DisplayName("Seething Anger gives the target creature +3/+0")
    void boostsTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeethingAnger()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears(player1))).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears(player1))).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeethingAnger()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears(player1))).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears(player1))).isEqualTo(2);
    }

    @Test
    @DisplayName("Paying buyback returns Seething Anger to its owner's hand")
    void buybackReturnsToHand() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeethingAnger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorceryWithBuyback(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Seething Anger");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .doesNotContain("Seething Anger");
        assertThat(gqs.getEffectivePower(gd, bears(player1))).isEqualTo(5);
    }

    @Test
    @DisplayName("Seething Anger cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new SeethingAnger()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent bears(Player player) {
        return findPermanent(player, "Grizzly Bears");
    }
}
