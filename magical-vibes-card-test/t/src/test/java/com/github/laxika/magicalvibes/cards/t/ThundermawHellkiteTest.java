package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.s.SafePassage;
import com.github.laxika.magicalvibes.cards.w.WelkinTern;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThundermawHellkite.class, AirElemental.class, GrizzlyBears.class, SuntailHawk.class,
        SafePassage.class, WelkinTern.class})
class ThundermawHellkiteTest extends BaseCardTest {

    @Test
    @DisplayName("Entering deals 1 damage to each flying creature opponents control and taps them")
    void entersDamagesAndTapsOpponentFliers() {
        Permanent oppFlier = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent oppGround = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownFlier = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        castHellkite();

        assertThat(oppFlier.getMarkedDamage()).isEqualTo(1);
        assertThat(oppFlier.isTapped()).isTrue();
        assertThat(oppGround.getMarkedDamage()).isZero();
        assertThat(oppGround.isTapped()).isFalse();
        assertThat(ownFlier.getMarkedDamage()).isZero();
        assertThat(ownFlier.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A 1/1 flier an opponent controls dies to the enter trigger")
    void killsOneToughnessFliers() {
        harness.addToBattlefield(player2, new SuntailHawk());

        castHellkite();

        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Preventing the damage does not prevent tapping the opponent's flier")
    void tapsFlierEvenWhenDamageIsPrevented() {
        Permanent flier = harness.addToBattlefieldAndReturn(player2, new WelkinTern());
        harness.setHand(player2, List.of(new SafePassage()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new ThundermawHellkite()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castAndResolveInstant(player2, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Welkin Tern");
        assertThat(flier.getMarkedDamage()).isZero();
        assertThat(flier.isTapped()).isTrue();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The trigger damages every opposing flier, including ones already tapped")
    void damagesTappedAndUntappedFliers() {
        Permanent tappedFlier = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        tappedFlier.tap();
        Permanent untappedFlier = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castHellkite();

        assertThat(tappedFlier.getMarkedDamage()).isEqualTo(1);
        assertThat(untappedFlier.getMarkedDamage()).isEqualTo(1);
        assertThat(tappedFlier.isTapped()).isTrue();
        assertThat(untappedFlier.isTapped()).isTrue();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The trigger resolves without opposing fliers and leaves ground creatures alone")
    void resolvesWithoutOpponentFliers() {
        Permanent groundCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castHellkite();

        harness.assertOnBattlefield(player1, "Thundermaw Hellkite");
        assertThat(groundCreature.getMarkedDamage()).isZero();
        assertThat(groundCreature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void castHellkite() {
        harness.setHand(player1, List.of(new ThundermawHellkite()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
