package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.Greed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ToxicDeluge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CourageousResolve.class, GrizzlyBears.class, Shock.class, Greed.class, ToxicDeluge.class})
class CourageousResolveTest extends BaseCardTest {

    @Test
    @DisplayName("Protects an optional target creature and draws a card")
    void protectsTargetAndDraws() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        castCourageousResolve(bear.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gqs.hasProtectionFromOpponents(gd, bear, player2.getId())).isTrue();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Fateful hour prevents the controller's life loss and game loss")
    void fatefulHour() {
        harness.setLife(player1, 5);
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        castCourageousResolve();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gqs.canPlayerLoseLife(gd, player1.getId())).isFalse();
        assertThat(gqs.canPlayerLoseLife(gd, player2.getId())).isTrue();
        assertThat(gqs.canPlayerLoseGame(gd, player1.getId())).isFalse();
        assertThat(gqs.canPlayerLoseGame(gd, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("Fateful hour expires at the end of the turn")
    void fatefulHourExpiresAtEndOfTurn() {
        harness.setLife(player1, 5);
        castCourageousResolve();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.canPlayerLoseLife(gd, player1.getId())).isTrue();
        assertThat(gqs.canPlayerLoseGame(gd, player1.getId())).isTrue();
        assertThat(gqs.canPlayerLoseGame(gd, player2.getId())).isTrue();
    }

    @Test
    @DisplayName("Fateful hour stops its controller from paying life to activate an ability")
    void fatefulHourStopsPayingLifeForAnAbility() {
        harness.setLife(player1, 5);
        castCourageousResolve();
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new Greed());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 5);
    }

    @Test
    @DisplayName("Fateful hour stops its controller from paying life to cast a spell")
    void fatefulHourStopsPayingLifeForASpell() {
        harness.setLife(player1, 5);
        castCourageousResolve();
        harness.setHand(player1, List.of(new ToxicDeluge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 5);
    }

    @Test
    @DisplayName("Fateful hour leaves the opponent free to pay life for an ability")
    void opponentMayStillPayLife() {
        harness.setLife(player1, 5);
        castCourageousResolve();
        harness.addToBattlefield(player2, new Greed());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, null);

        harness.assertLife(player2, 18);
    }

    private void castCourageousResolve() {
        castCourageousResolve(null);
    }

    private void castCourageousResolve(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new CourageousResolve()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        if (targetId == null) {
            harness.castInstant(player1, 0);
        } else {
            harness.castInstant(player1, 0, targetId);
        }
        harness.passBothPriorities();
    }
}
