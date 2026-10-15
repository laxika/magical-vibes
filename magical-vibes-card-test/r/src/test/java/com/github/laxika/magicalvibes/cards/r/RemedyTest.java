package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Remedy.class, GiantSpider.class, Plains.class, Shock.class, Unsummon.class})
class RemedyTest extends BaseCardTest {

    @Test
    void dividesFivePreventionAmongCreaturesAndBothPlayers() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new Remedy()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, Map.of(first.getId(), 1, second.getId(), 2,
                player1.getId(), 1, player2.getId(), 1));
        harness.passBothPriorities();

        assertThat(first.getDamagePreventionShield()).isEqualTo(1);
        assertThat(second.getDamagePreventionShield()).isEqualTo(2);
        assertThat(gd.playerDamagePreventionShields).containsEntry(player1.getId(), 1)
                .containsEntry(player2.getId(), 1);
        harness.assertInGraveyard(player1, "Remedy");
    }

    @Test
    void playerShieldIsConsumedAcrossDamageEventsAndDoesNotPreventExcess() {
        harness.setHand(player1, List.of(new Remedy(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, Map.of(player2.getId(), 5));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 20);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 20);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 19);
    }

    @Test
    void creatureShieldPreventsOnlyItsAssignedShare() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new Remedy(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, Map.of(spider.getId(), 3, player2.getId(), 2));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, spider.getId());
        assertThat(spider.getMarkedDamage()).isZero();
        harness.castAndResolveInstant(player1, 0, spider.getId());
        assertThat(spider.getMarkedDamage()).isEqualTo(1);
        assertThat(spider.getDamagePreventionShield()).isZero();
        assertThat(gd.playerDamagePreventionShields).containsEntry(player2.getId(), 2);
    }

    @Test
    void remainingTargetKeepsItsShareWhenAnotherTargetLeaves() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new Remedy(), new Unsummon(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, Map.of(spider.getId(), 4, player2.getId(), 1));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, spider.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 19);
        assertThat(spider.getDamagePreventionShield()).isZero();
    }

    @Test
    void unusedShieldsExpireAtEndOfTurn() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new Remedy()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, Map.of(spider.getId(), 3, player2.getId(), 2));
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(spider.getDamagePreventionShield()).isZero();
        assertThat(gd.playerDamagePreventionShields).isEmpty();
    }

    @Test
    void mayChooseZeroTargets() {
        harness.setHand(player1, List.of(new Remedy()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, Map.<UUID, Integer>of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Remedy");
        assertThat(gd.playerDamagePreventionShields).isEmpty();
    }

    @Test
    void nonemptyAssignmentsMustTotalFive() {
        harness.setHand(player1, List.of(new Remedy()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(player2.getId(), 4)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void eachChosenTargetMustReceivePositivePrevention() {
        harness.setHand(player1, List.of(new Remedy()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                Map.of(player1.getId(), 0, player2.getId(), 5)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetANoncreatureLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new Remedy()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(land.getId(), 5)))
                .isInstanceOf(IllegalStateException.class);
    }
}
