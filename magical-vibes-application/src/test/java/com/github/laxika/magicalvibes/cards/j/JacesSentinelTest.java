package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JacesSentinelTest extends BaseCardTest {

    // ===== Conditional +1/+0 and can't be blocked with Jace =====

    @Test
    @DisplayName("Gets +1/+0 when controller controls a Jace planeswalker")
    void getsPowerBoostWithJace() {
        harness.addToBattlefield(player1, new JacesSentinel());
        harness.addToBattlefield(player1, createJacePlaneswalker());

        Permanent sentinel = findPermanent(player1, "Jace's Sentinel");
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(2); // 1 base + 1 bonus
    }

    @Test
    @DisplayName("Can't be blocked when controller controls a Jace planeswalker")
    void cantBeBlockedWithJace() {
        harness.addToBattlefield(player1, new JacesSentinel());
        harness.addToBattlefield(player1, createJacePlaneswalker());

        Permanent sentinel = findPermanent(player1, "Jace's Sentinel");
        assertThat(gqs.hasCantBeBlocked(gd, sentinel)).isTrue();
    }

    // ===== No bonus without a Jace =====

    @Test
    @DisplayName("No power boost without a Jace planeswalker")
    void noPowerBoostWithoutJace() {
        harness.addToBattlefield(player1, new JacesSentinel());

        Permanent sentinel = findPermanent(player1, "Jace's Sentinel");
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(1); // 1 base, no bonus
    }

    @Test
    @DisplayName("Can be blocked without a Jace planeswalker")
    void canBeBlockedWithoutJace() {
        harness.addToBattlefield(player1, new JacesSentinel());

        Permanent sentinel = findPermanent(player1, "Jace's Sentinel");
        assertThat(gqs.hasCantBeBlocked(gd, sentinel)).isFalse();
    }

    // ===== Non-Jace creature doesn't count =====

    @Test
    @DisplayName("Non-Jace creature does not grant bonus")
    void nonJaceDoesNotGrantBonus() {
        harness.addToBattlefield(player1, new JacesSentinel());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent sentinel = findPermanent(player1, "Jace's Sentinel");
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, sentinel)).isFalse();
    }

    // ===== Loses bonus when Jace leaves =====

    @Test
    @DisplayName("Loses +1/+0 and can't be blocked when Jace leaves the battlefield")
    void losesBonusWhenJaceLeaves() {
        harness.addToBattlefield(player1, new JacesSentinel());
        harness.addToBattlefield(player1, createJacePlaneswalker());

        Permanent sentinel = findPermanent(player1, "Jace's Sentinel");
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(2);
        assertThat(gqs.hasCantBeBlocked(gd, sentinel)).isTrue();

        // Remove the Jace planeswalker
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getSubtypes().contains(CardSubtype.JACE));

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, sentinel)).isFalse();
    }

    // ===== Opponent's Jace doesn't count =====

    @Test
    @DisplayName("Opponent's Jace planeswalker does not grant bonus")
    void opponentJaceDoesNotCount() {
        harness.addToBattlefield(player1, new JacesSentinel());
        harness.addToBattlefield(player2, createJacePlaneswalker());

        Permanent sentinel = findPermanent(player1, "Jace's Sentinel");
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, sentinel)).isFalse();
    }

    // ===== Helper methods =====

    private Card createJacePlaneswalker() {
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(CardSubtype.JACE));
        return card;
    }

}
