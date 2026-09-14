package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HengeGuardian;
import com.github.laxika.magicalvibes.cards.j.JhovallQueen;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Crackdown.class, CinderElemental.class, HengeGuardian.class, JhovallQueen.class})
class CrackdownTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped nonwhite creature with power 3 or greater does not untap")
    void power3NonwhiteCreatureStaysTapped() {
        addCreatureReady(player1, new Crackdown());
        Permanent giant = addCreatureReady(player1, new HengeGuardian());
        giant.tap();

        advanceToUpkeep(player1);

        assertThat(giant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped nonwhite creature with power under 3 untaps normally")
    void power2NonwhiteCreatureUntaps() {
        addCreatureReady(player1, new Crackdown());
        Permanent bears = addCreatureReady(player1, new CinderElemental());
        bears.tap();

        advanceToUpkeep(player1);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapped white creature with power 3 or greater untaps normally")
    void whiteCreatureUntaps() {
        addCreatureReady(player1, new Crackdown());
        Permanent angel = addCreatureReady(player1, new JhovallQueen());
        angel.tap();

        advanceToUpkeep(player1);

        assertThat(angel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Crackdown affects nonwhite creatures during an opponent's untap step")
    void affectsOpponentCreatures() {
        addCreatureReady(player1, new Crackdown());
        Permanent opponentGiant = addCreatureReady(player2, new HengeGuardian());
        opponentGiant.tap();

        advanceToUpkeep(player2);

        assertThat(opponentGiant.isTapped()).isTrue();
    }
}
