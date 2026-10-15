package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StingingShot.class, SuntailHawk.class, AirElemental.class, GrizzlyBears.class})
class StingingShotTest extends BaseCardTest {

    @Test
    @DisplayName("Puts three -1/-1 counters on a flying creature, killing a small one")
    void killsSmallFlyer() {
        harness.addToBattlefield(player2, new SuntailHawk());
        UUID hawkId = harness.getPermanentId(player2, "Suntail Hawk");

        harness.setHand(player1, List.of(new StingingShot()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, hawkId);

        // Suntail Hawk (1/1) with 3 -1/-1 counters dies to SBA
        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Larger flyer survives with three -1/-1 counters")
    void largeFlyerSurvives() {
        harness.addToBattlefield(player2, new AirElemental());
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.setHand(player1, List.of(new StingingShot()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        // Air Elemental (4/4) with 3 -1/-1 counters → 1/1
        Permanent target = findPermanent(player2, "Air Elemental");
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyer() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new StingingShot()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetOwnFlyingCreature() {
        harness.addToBattlefield(player1, new AirElemental());
        UUID targetId = harness.getPermanentId(player1, "Air Elemental");
        harness.setHand(player1, List.of(new StingingShot()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(findPermanent(player1, "Air Elemental")
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Stinging Shot");
    }

    @Test
    void doesNotAffectAnotherCreatureWhenTargetLeavesBattlefield() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new SuntailHawk());
        Permanent target = findPermanent(player2, "Air Elemental");
        harness.setHand(player1, List.of(new StingingShot()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Suntail Hawk")
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Stinging Shot");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cyclingDiscardsImmediatelyAndDrawsOnResolutionWithoutFlyingTargets() {
        harness.setHand(player1, List.of(new StingingShot()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Stinging Shot");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new StingingShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Stinging Shot");
        harness.assertNotInGraveyard(player1, "Stinging Shot");
        assertThat(gd.stack).isEmpty();
    }
}
