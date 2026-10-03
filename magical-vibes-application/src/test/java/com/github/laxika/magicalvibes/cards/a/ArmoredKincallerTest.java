package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MineshaftSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmoredKincaller.class, MineshaftSpider.class})
class ArmoredKincallerTest extends BaseCardTest {

    @Test
    void gainsLifeWhenDinosaurIsRevealed() {
        castWithHand(new ArmoredKincaller());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    void doesNotGainLifeWhenRevealIsDeclinedWithoutAnotherDinosaur() {
        castWithHand(new ArmoredKincaller());

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void gainsLifeWithAnotherDinosaurEvenWithoutReveal() {
        harness.addToBattlefield(player1, new ArmoredKincaller());
        castWithHand(new MineshaftSpider());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    void gainsLifeOnlyOnceWhenRevealingWithAnotherDinosaur() {
        harness.addToBattlefield(player1, new ArmoredKincaller());
        castWithHand(new ArmoredKincaller());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    void doesNotGainLifeWithoutRevealOrAnotherDinosaur() {
        castWithHand(new MineshaftSpider());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void acceptingRevealMakesTheDinosaurPublicAndLeavesItInHand() {
        ArmoredKincaller dinosaur = new ArmoredKincaller();
        castWithHand(dinosaur);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(dinosaur);
        assertThat(gameLogContains("reveals")).isTrue();
    }

    @Test
    void gainsLifeWhenRevealIsDeclinedWithAnotherDinosaur() {
        harness.addToBattlefield(player1, new ArmoredKincaller());
        castWithHand(new ArmoredKincaller());

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void opponentsDinosaurDoesNotEnableLifeGain() {
        harness.addToBattlefield(player2, new ArmoredKincaller());
        castWithHand(new MineshaftSpider());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void emptyHandDoesNotEnableLifeGain() {
        harness.setHand(player1, List.of(new ArmoredKincaller()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castWithHand(com.github.laxika.magicalvibes.model.Card cardInHand) {
        harness.setHand(player1, List.of(new ArmoredKincaller(), cardInHand));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
