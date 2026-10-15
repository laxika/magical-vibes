package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.FathomSeer;
import com.github.laxika.magicalvibes.cards.g.Greenseeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpellBurst.class, AshcoatBear.class, Greenseeker.class, Cancel.class, FathomSeer.class})
class SpellBurstTest extends BaseCardTest {

    @Test
    void countersTargetSpellWithManaValueEqualToX() {
        AshcoatBear bears = new AshcoatBear();
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
        Greenseeker elves = new Greenseeker();
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
        AshcoatBear bears = new AshcoatBear();
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
        AshcoatBear bears = new AshcoatBear();
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

    @Test
    void countersFaceDownSpellWithXZero() {
        FathomSeer seer = new FathomSeer();
        SpellBurst burst = new SpellBurst();
        harness.setHand(player1, List.of(seer));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player2, List.of(burst));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreatureWithMorph(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, seer.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(seer);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(burst);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotTargetFaceDownSpellUsingItsPrintedManaValue() {
        FathomSeer seer = new FathomSeer();
        harness.setHand(player1, List.of(seer));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player2, List.of(new SpellBurst()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 2, seer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void targetManaValueIncludesXButNotBuybackAndCounteredBuybackIsNotReturned() {
        AshcoatBear bears = new AshcoatBear();
        SpellBurst boughtBackBurst = new SpellBurst();
        SpellBurst counter = new SpellBurst();
        harness.setHand(player1, List.of(bears, counter));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setHand(player2, List.of(boughtBackBurst));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        castWithBuybackForX(player2, bears.getId());
        harness.castInstant(player1, 0, 3, boughtBackBurst.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(boughtBackBurst);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(counter);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId()).containsExactly(bears.getId());
    }
}
