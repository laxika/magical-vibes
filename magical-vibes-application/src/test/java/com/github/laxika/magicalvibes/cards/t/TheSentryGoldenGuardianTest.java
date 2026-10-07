package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TheSentryGoldenGuardian.class)
class TheSentryGoldenGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes the targeted opponent create The Void")
    void createsTheVoidForTargetedOpponent() {
        castAndResolveSentry();

        Permanent theVoid = findPermanent(player2, "The Void");
        assertThat(theVoid.getCard().isToken()).isTrue();
        assertThat(theVoid.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.HORROR, CardSubtype.VILLAIN);
        assertThat(theVoid.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(gqs.getEffectivePower(gd, theVoid)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, theVoid)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, theVoid, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, theVoid, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The Void must attack each combat if able")
    void theVoidMustAttackEachCombat() {
        castAndResolveSentry();
        Permanent theVoid = findPermanent(player2, "The Void");
        theVoid.setSummoningSick(false);

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("ETB target must be an opponent")
    void targetMustBeOpponent() {
        harness.setHand(player1, List.of(new TheSentryGoldenGuardian()));
        addSentryMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("The Void is created only when the enters trigger resolves")
    void tokenCreationWaitsForTriggerResolution() {
        harness.setHand(player1, List.of(new TheSentryGoldenGuardian()));
        addSentryMana();
        harness.castCreature(player1, 0, player2.getId());

        assertThat(countPermanents(player2, "The Void")).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Sentry, Golden Guardian");
        assertThat(countPermanents(player2, "The Void")).isZero();
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "The Void")).isEqualTo(1);
        assertThat(countPermanents(player1, "The Void")).isZero();
    }

    @Test
    @DisplayName("A summoning-sick Void is not required to attack")
    void summoningSickVoidMayStayBack() {
        castAndResolveSentry();
        Permanent theVoid = findPermanent(player2, "The Void");

        declareAttackers(player2, List.of());

        assertThat(theVoid.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("A tapped Void is not required to attack")
    void tappedVoidMayStayBack() {
        castAndResolveSentry();
        Permanent theVoid = findPermanent(player2, "The Void");
        theVoid.setSummoningSick(false);
        theVoid.tap();

        declareAttackers(player2, List.of());

        assertThat(theVoid.isAttacking()).isFalse();
    }

    private void castAndResolveSentry() {
        harness.setHand(player1, List.of(new TheSentryGoldenGuardian()));
        addSentryMana();
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addSentryMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
