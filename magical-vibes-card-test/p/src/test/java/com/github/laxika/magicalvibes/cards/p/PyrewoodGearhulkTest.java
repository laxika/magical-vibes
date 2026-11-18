package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Pyrewood Gearhulk")
@CardUsed({PyrewoodGearhulk.class, GrizzlyBears.class, Shock.class})
class PyrewoodGearhulkTest extends BaseCardTest {

    private void castGearhulk() {
        harness.castFromHand(player1, new PyrewoodGearhulk(), "{2}{R}{R}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB boosts and grants vigilance and menace to other creatures you control")
    void etbBoostsOtherOwnCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castGearhulk();

        Permanent gearhulk = findPermanent(player1, "Pyrewood Gearhulk");
        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isTrue();
        assertThat(PermanentGrants.powerBoostOf(gd, gearhulk)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, gearhulk)).isZero();
        assertThat(PermanentGrants.powerBoostOf(gd, opponentBears)).isZero();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("ETB boost and keyword grants wear off at end of turn")
    void etbEffectsWearOff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castGearhulk();
        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, bears)).isZero();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("ETB makes damage unpreventable for the rest of the turn")
    void damageCannotBePreventedThisTurn() {
        gd.addPlayerDamagePreventionShield(player2.getId(), 10);
        castGearhulk();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDamagePreventionShields().get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Creatures entering after the trigger resolves do not receive its bonuses")
    void laterCreaturesAreNotAffected() {
        castGearhulk();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, bears)).isZero();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("A second Gearhulk boosts the first Gearhulk and stacks bonuses on other creatures")
    void secondGearhulkBoostsOtherCreaturesAgain() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castGearhulk();
        Permanent firstGearhulk = findPermanent(player1, "Pyrewood Gearhulk");

        castGearhulk();

        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isEqualTo(4);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bears)).isEqualTo(4);
        assertThat(PermanentGrants.powerBoostOf(gd, firstGearhulk)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, firstGearhulk)).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage remains preventable while the enters trigger is on the stack")
    void preventionStopsOnlyWhenTriggerResolves() {
        gd.addPlayerDamagePreventionShield(player2.getId(), 10);
        harness.castFromHand(player1, new PyrewoodGearhulk(), "{2}{R}{R}{G}{G}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
        assertThat(gd.playerDamagePreventionShields().get(player2.getId())).isEqualTo(8);

        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        assertThat(gd.playerDamagePreventionShields().get(player2.getId())).isEqualTo(8);
    }

    @Test
    @DisplayName("The opponent's damage also cannot be prevented")
    void opponentDamageCannotBePrevented() {
        castGearhulk();
        gd.addPlayerDamagePreventionShield(player1.getId(), 10);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
        assertThat(gd.playerDamagePreventionShields().get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Damage to creatures bypasses prevention shields too")
    void creatureDamageCannotBePrevented() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.addPermanentDamagePreventionShield(bears.getId(), 10);
        castGearhulk();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Damage can be prevented again on the next turn")
    void damagePreventionReturnsNextTurn() {
        castGearhulk();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        gd.addPlayerDamagePreventionShield(player1.getId(), 10);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 20);
        assertThat(gd.playerDamagePreventionShields().get(player1.getId())).isEqualTo(8);
    }
}
