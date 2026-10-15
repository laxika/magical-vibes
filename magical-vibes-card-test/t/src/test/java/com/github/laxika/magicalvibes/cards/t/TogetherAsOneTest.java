package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TogetherAsOne.class, GrizzlyBears.class, Shock.class})
class TogetherAsOneTest extends BaseCardTest {

    @Test
    @DisplayName("Converge counts distinct colored mana and resolves all three effects")
    void resolvesDrawDamageAndLifeGain() {
        Permanent damageTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.setHand(player1, List.of(new TogetherAsOne()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0, List.of(player2.getId(), damageTarget.getId()));
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Repeated mana of one color counts only once for Converge")
    void repeatedColorCountsOnce() {
        Permanent damageTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Shock(), new Shock()));
        harness.setHand(player1, List.of(new TogetherAsOne()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, List.of(player2.getId(), damageTarget.getId()));
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(damageTarget.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Rejects a non-player draw target")
    void rejectsNonPlayerDrawTarget() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TogetherAsOne()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(permanent.getId(), permanent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Colorless payment produces no draw, damage, or life gain")
    void colorlessPaymentCountsZeroColors() {
        Permanent damageTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Shock()));
        harness.setHand(player1, List.of(new TogetherAsOne()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId(), damageTarget.getId()));

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(damageTarget.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, lifeBefore);
        harness.assertInGraveyard(player1, "Together as One");
    }

    @Test
    @DisplayName("All five colors draw for the chosen player and damage a different player")
    void fiveColorsResolveForSeparatePlayers() {
        harness.setLibrary(player1, List.of(new TogetherAsOne(), new TogetherAsOne(),
                new TogetherAsOne(), new TogetherAsOne(), new TogetherAsOne()));
        harness.setHand(player1, List.of(new TogetherAsOne()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int casterLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId(), player2.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player1, casterLife + 5);
        harness.assertLife(player2, opponentLife - 5);
    }

    @Test
    @DisplayName("The same player can be selected for both target clauses")
    void samePlayerCanDrawAndTakeDamage() {
        harness.setLibrary(player2, List.of(new TogetherAsOne(), new TogetherAsOne()));
        harness.setHand(player1, List.of(new TogetherAsOne()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 3);
        int casterLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId(), player2.getId()));

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertLife(player1, casterLife + 2);
        harness.assertLife(player2, opponentLife - 2);
    }

    @Test
    @DisplayName("Draw and life gain still resolve when the damage target leaves the battlefield")
    void missingDamageTargetDoesNotStopOtherEffects() {
        Permanent damageTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new TogetherAsOne()));
        harness.setHand(player1, List.of(new TogetherAsOne()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player2, ManaColor.RED, 1);
        int casterLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, List.of(player2.getId(), damageTarget.getId()));
        harness.castAndResolveInstant(player2, 0, damageTarget.getId());
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertLife(player1, casterLife + 1);
        harness.assertLife(player2, opponentLife);
        harness.assertInGraveyard(player1, "Together as One");
    }
}
