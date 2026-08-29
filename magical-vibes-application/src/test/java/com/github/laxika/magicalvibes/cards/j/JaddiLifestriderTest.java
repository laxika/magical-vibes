package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JaddiLifestriderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps the chosen creatures and gains 2 life for each")
    void entersAndGainsLifeForChosenCreatures() {
        harness.setLife(player1, 20);
        Permanent grizzlyBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castJaddiLifestrider();

        Permanent jaddiLifestrider = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Jaddi Lifestrider"))
                .findFirst().orElseThrow();
        harness.handleMultiplePermanentsChosen(player1, List.of(grizzlyBears.getId(), jaddiLifestrider.getId()));

        assertThat(grizzlyBears.isTapped()).isTrue();
        assertThat(jaddiLifestrider.isTapped()).isTrue();
        assertThat(opponentCreature.isTapped()).isFalse();
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("ETB may tap no creatures")
    void mayTapNoCreatures() {
        harness.setLife(player1, 20);
        Permanent grizzlyBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castJaddiLifestrider();

        Permanent jaddiLifestrider = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Jaddi Lifestrider"))
                .findFirst().orElseThrow();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(grizzlyBears.isTapped()).isFalse();
        assertThat(jaddiLifestrider.isTapped()).isFalse();
        harness.assertLife(player1, 20);
    }

    private void castJaddiLifestrider() {
        harness.setHand(player1, List.of(new JaddiLifestrider()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
