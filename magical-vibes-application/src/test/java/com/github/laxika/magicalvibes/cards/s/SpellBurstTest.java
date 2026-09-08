package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpellBurst.class, GrizzlyBears.class, LlanowarElves.class, Cancel.class})
class SpellBurstTest extends BaseCardTest {

    @Test
    void countersTargetSpellWithManaValueEqualToX() {
        GrizzlyBears bears = new GrizzlyBears();
        SpellBurst burst = new SpellBurst();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(burst));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 2, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(burst);
    }

    @Test
    void cannotTargetSpellWithDifferentManaValue() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new SpellBurst()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 2, elves.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void payingBuybackReturnsSpellToHandAsItResolves() {
        GrizzlyBears bears = new GrizzlyBears();
        SpellBurst burst = new SpellBurst();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(burst));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        castWithBuybackForX(player2, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(burst);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears);
    }

    @Test
    void fizzledBuybackSpellGoesToGraveyard() {
        GrizzlyBears bears = new GrizzlyBears();
        SpellBurst burst = new SpellBurst();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(burst));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        castWithBuybackForX(player2, bears.getId());

        Cancel cancel = new Cancel();
        harness.setHand(player1, List.of(cancel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(burst);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears, cancel);
    }

    private void castWithBuybackForX(com.github.laxika.magicalvibes.model.Player player, java.util.UUID targetId) {
        gs.playCard(gd, player, 0, 2, targetId, null, List.of(), List.of(), false, null, null, List.of(), null,
                List.of(), false, null, List.of(), null, null, List.of(), true);
    }

    @Test
    void cannotTargetAPlayer() {
        SpellBurst burst = new SpellBurst();
        harness.setHand(player1, List.of(burst));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
