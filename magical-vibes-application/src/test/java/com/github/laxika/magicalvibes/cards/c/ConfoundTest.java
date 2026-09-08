package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfoundTest extends BaseCardTest {

    @Test
    void canTargetASpellThatTargetsACreature() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        MightOfOaks mightOfOaks = new MightOfOaks();
        harness.setHand(player1, List.of(mightOfOaks));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);

        Confound confound = new Confound();
        harness.setHand(player2, List.of(confound));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, mightOfOaks.getId());

        assertThat(harness.getGameData().stack).hasSize(2);
        assertThat(harness.getGameData().stack.getLast().getTargetId()).isEqualTo(mightOfOaks.getId());
    }

    @Test
    void cannotTargetACreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Confound()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countersTargetSpellAndDrawsACard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        MightOfOaks mightOfOaks = new MightOfOaks();
        harness.setHand(player1, List.of(mightOfOaks));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player1);

        Confound confound = new Confound();
        harness.setHand(player2, List.of(confound));
        harness.addMana(player2, ManaColor.BLUE, 2);
        int handSizeBeforeCasting = gd.playerHands.get(player2.getId()).size();
        harness.castInstant(player2, 0, mightOfOaks.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Might of Oaks");
        harness.assertNotOnBattlefield(player1, "Might of Oaks");
        harness.assertInGraveyard(player2, "Confound");
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handSizeBeforeCasting);
    }
}
