package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlackCarriage;
import com.github.laxika.magicalvibes.cards.b.BeastWalkers;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpectralBears.class, BeastWalkers.class, BlackCarriage.class})
class SpectralBearsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking into a defender with no black permanents locks Spectral Bears' next untap")
    void locksUntapWhenDefenderHasNoBlackPermanents() {
        Permanent bears = addCreatureReady(player1, new SpectralBears());
        addCreatureReady(player2, new BeastWalkers());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("No untap lock when the defending player controls a black nontoken permanent")
    void noLockWhenDefenderHasBlackNontokenPermanent() {
        Permanent bears = addCreatureReady(player1, new SpectralBears());
        addCreatureReady(player2, new BlackCarriage());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(bears.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("A black token the defender controls does not stop the untap lock")
    void blackTokenDoesNotCount() {
        Permanent bears = addCreatureReady(player1, new SpectralBears());
        Card token = createCreature("Black Token", 1, 1, CardColor.BLACK);
        token.setToken(true);
        harness.addToBattlefield(player2, token);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the defending player's permanents matter — the attacker's own black permanent is ignored")
    void controllersBlackPermanentIsIgnored() {
        Permanent bears = addCreatureReady(player1, new SpectralBears());
        addCreatureReady(player1, new BlackCarriage());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability triggers when the defending player controls no permanents")
    void locksUntapWhenDefenderControlsNoPermanents() {
        Permanent bears = addCreatureReady(player1, new SpectralBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("The lock lasts through only the next untap step")
    void locksOnlyNextUntapStep() {
        Permanent bears = addCreatureReady(player1, new SpectralBears());
        addCreatureReady(player2, new BeastWalkers());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.performUntapStep(player1);
        assertThat(bears.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The condition is checked again if a black nontoken permanent enters before resolution")
    void conditionLostBeforeResolutionDoesNotLockBears() {
        Permanent bears = addCreatureReady(player1, new SpectralBears());

        declareAttackers(player1, List.of(0));
        addCreatureReady(player2, new BlackCarriage());
        resolveAllTriggers();

        assertThat(bears.getSkipUntapCount()).isZero();
    }

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
