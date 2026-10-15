package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
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

@CardUsed({JolraelsCentaur.class, Incinerate.class, IronTuskElephant.class})
class JolraelsCentaurTest extends BaseCardTest {

    @Test
    @DisplayName("Shroud prevents Jolrael's Centaur from being targeted")
    void shroudPreventsTargeting() {
        Permanent centaur = addCreatureReady(player2, new JolraelsCentaur());
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, centaur.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Flanking gives a non-flanking blocker -1/-1 until end of turn")
    void flankingWeakensNonFlankingBlocker() {
        Permanent centaur = addCreatureReady(player1, new JolraelsCentaur());
        centaur.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new IronTuskElephant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Shroud also prevents the controller from targeting Jolrael's Centaur")
    void shroudPreventsControllerTargeting() {
        Permanent centaur = addCreatureReady(player1, new JolraelsCentaur());
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, centaur.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Flanking does not trigger against a blocker with flanking")
    void flankingDoesNotTriggerAgainstFlankingBlocker() {
        Permanent centaur = addCreatureReady(player1, new JolraelsCentaur());
        centaur.setAttacking(true);
        addCreatureReady(player2, new JolraelsCentaur());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flanking weakens each non-flanking blocker separately")
    void flankingWeakensEveryNonFlankingBlocker() {
        Permanent centaur = addCreatureReady(player1, new JolraelsCentaur());
        centaur.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new IronTuskElephant());
        Permanent secondBlocker = addCreatureReady(player2, new IronTuskElephant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(firstBlocker.getEffectivePower()).isEqualTo(2);
        assertThat(firstBlocker.getEffectiveToughness()).isEqualTo(2);
        assertThat(secondBlocker.getEffectivePower()).isEqualTo(2);
        assertThat(secondBlocker.getEffectiveToughness()).isEqualTo(2);
    }
}
