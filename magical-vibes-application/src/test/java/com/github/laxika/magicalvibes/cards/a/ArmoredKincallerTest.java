package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FrenziedRaptor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmoredKincaller.class, FrenziedRaptor.class, GrizzlyBears.class})
class ArmoredKincallerTest extends BaseCardTest {

    @Test
    void gainsLifeWhenDinosaurIsRevealed() {
        castWithHand(new FrenziedRaptor());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    void doesNotGainLifeWhenRevealIsDeclinedWithoutAnotherDinosaur() {
        castWithHand(new FrenziedRaptor());

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void gainsLifeWithAnotherDinosaurEvenWithoutReveal() {
        harness.addToBattlefield(player1, new FrenziedRaptor());
        castWithHand(new GrizzlyBears());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    void gainsLifeOnlyOnceWhenRevealingWithAnotherDinosaur() {
        harness.addToBattlefield(player1, new FrenziedRaptor());
        castWithHand(new FrenziedRaptor());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    void doesNotGainLifeWithoutRevealOrAnotherDinosaur() {
        castWithHand(new GrizzlyBears());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    private void castWithHand(com.github.laxika.magicalvibes.model.Card cardInHand) {
        harness.setHand(player1, List.of(new ArmoredKincaller(), cardInHand));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
