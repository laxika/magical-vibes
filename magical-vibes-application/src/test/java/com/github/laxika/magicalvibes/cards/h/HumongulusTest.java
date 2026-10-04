package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.k.KayasWrath;
import com.github.laxika.magicalvibes.cards.s.Scorchmark;
import com.github.laxika.magicalvibes.cards.s.StonyStrength;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Humongulus.class, Scorchmark.class, StonyStrength.class, KayasWrath.class})
class HumongulusTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent cannot target Humongulus with a spell")
    void opponentCannotTargetWithSpell() {
        Permanent humongulus = addCreatureReady(player1, new Humongulus());

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Scorchmark()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, humongulus.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Controller can target Humongulus with a spell")
    void controllerCanTargetWithSpell() {
        Permanent humongulus = addCreatureReady(player1, new Humongulus());

        harness.setHand(player1, List.of(new StonyStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, humongulus.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Hexproof does not stop an opponent's untargeted destruction")
    void opponentCanDestroyWithoutTargeting() {
        addCreatureReady(player2, new Humongulus());
        harness.setHand(player1, List.of(new KayasWrath()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player2, "Humongulus");
        harness.assertInGraveyard(player2, "Humongulus");
    }
}
