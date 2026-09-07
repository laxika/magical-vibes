package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HealingSalve;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RazorRings.class, GrizzlyBears.class, HealingSalve.class})
class RazorRingsTest extends BaseCardTest {

    @Test
    @DisplayName("deals 4 damage and gains life equal to excess damage")
    void gainsLifeForExcessDamage() {
        harness.forceActivePlayer(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setAttacking(true);

        castRazorRings(target);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("gains no life when there is no excess damage")
    void gainsNoLifeWithoutExcessDamage() {
        harness.forceActivePlayer(player1);
        GrizzlyBears targetCard = new GrizzlyBears();
        targetCard.setToughness(5);
        Permanent target = addCreatureReady(player2, targetCard);
        target.setBlocking(true);

        castRazorRings(target);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("counts only damage that was not prevented")
    void gainsLifeOnlyForDamageActuallyDealt() {
        harness.forceActivePlayer(player1);
        GrizzlyBears targetCard = new GrizzlyBears();
        targetCard.setToughness(5);
        Permanent target = addCreatureReady(player2, targetCard);
        target.setBlocking(true);

        harness.setHand(player1, List.of(new HealingSalve()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, 1, target.getId());
        harness.passBothPriorities();

        castRazorRings(target);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("cannot target a creature that is neither attacking nor blocking")
    void rejectsIdleCreature() {
        harness.forceActivePlayer(player1);
        Permanent idle = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new RazorRings()));
        addMana();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, idle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking or blocking creature");
    }

    private void castRazorRings(Permanent target) {
        harness.setHand(player1, List.of(new RazorRings()));
        addMana();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
