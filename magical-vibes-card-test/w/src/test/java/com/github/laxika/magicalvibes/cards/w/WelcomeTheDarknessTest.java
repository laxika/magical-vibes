package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RenewedFaith;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WelcomeTheDarkness.class, GrizzlyBears.class, RenewedFaith.class})
class WelcomeTheDarknessTest extends BaseCardTest {

    @Test
    void drawsCreatesDemonSetsLifeAndLocksLifeGain() {
        harness.setHand(player1, List.of(new WelcomeTheDarkness()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, 3, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        Permanent demon = findPermanent(player1, "Demon");
        assertThat(gqs.getEffectivePower(gd, demon)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, demon)).isEqualTo(3);
        harness.assertLife(player1, 3);
        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isFalse();
    }

    @Test
    void laterLifeGainIsStillPrevented() {
        harness.setHand(player1, List.of(new WelcomeTheDarkness(), new RenewedFaith()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 1);
    }

    @Test
    void canRaiseLifeBeforeItsLifeGainRestrictionBegins() {
        harness.setHand(player1, List.of(new WelcomeTheDarkness()));
        harness.setLibrary(player1, List.of(new WelcomeTheDarkness(), new WelcomeTheDarkness(),
                new WelcomeTheDarkness()));
        harness.setLife(player1, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, 3, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(countPermanents(player1, "Demon")).isEqualTo(1);
        Permanent demon = findPermanent(player1, "Demon");
        assertThat(gqs.getEffectivePower(gd, demon)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, demon)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, demon, Keyword.FLYING)).isTrue();
        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isFalse();
    }

    @Test
    void laterCastsCannotRaiseLifeButCanStillLowerIt() {
        harness.setHand(player1, List.of(new WelcomeTheDarkness(), new WelcomeTheDarkness(),
                new WelcomeTheDarkness()));
        harness.setLibrary(player1, List.of(new WelcomeTheDarkness(), new WelcomeTheDarkness(),
                new WelcomeTheDarkness(), new WelcomeTheDarkness(), new WelcomeTheDarkness(),
                new WelcomeTheDarkness()));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.assertLife(player1, 2);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, 3, null);
        harness.passBothPriorities();
        harness.assertLife(player1, 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(findPermanents(player1, "Demon"))
                .extracting(demon -> gqs.getEffectivePower(gd, demon)).containsExactly(2, 3);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();
        harness.assertLife(player1, 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        assertThat(countPermanents(player1, "Demon")).isEqualTo(3);
    }

    @Test
    void opponentCanStillGainLife() {
        harness.setHand(player1, List.of(new WelcomeTheDarkness()));
        harness.setLibrary(player1, List.of(new WelcomeTheDarkness()));
        harness.setHand(player2, List.of(new RenewedFaith()));
        harness.setLife(player2, 10);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0);

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 1);
        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isFalse();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isTrue();
    }
    @Test
    void rejectsZeroX() {
        harness.setHand(player1, List.of(new WelcomeTheDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
