package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.s.ScrollOfAvacyn;
import com.github.laxika.magicalvibes.cards.t.TibaltTheFiendBlooded;
import com.github.laxika.magicalvibes.cards.w.WanderingWolf;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BurnAtTheStake.class, WanderingWolf.class, ScrollOfAvacyn.class,
        Cloudshift.class, TibaltTheFiendBlooded.class})
class BurnAtTheStakeTest extends BaseCardTest {

    private void addMana(int amount) {
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, amount);
    }

    @Test
    @DisplayName("Deals three times the number of creatures tapped to the target player")
    void dealsThreeTimesTappedCreaturesToPlayer() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());

        harness.setHand(player1, List.of(new BurnAtTheStake()));
        addMana(2);

        harness.castSorceryTappingPermanents(player1, 0, player2.getId(),
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Kills a target creature when enough creatures are tapped")
    void killsTargetCreature() {
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new WanderingWolf());

        harness.setHand(player1, List.of(new BurnAtTheStake()));
        addMana(2);

        harness.castSorceryTappingPermanents(player1, 0, victim.getId(), List.of(tapped.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wandering Wolf");
        harness.assertInGraveyard(player2, "Wandering Wolf");
    }

    @Test
    @DisplayName("Tapping no creatures deals no damage")
    void tappingNoCreaturesDealsNoDamage() {
        harness.setHand(player1, List.of(new BurnAtTheStake()));
        addMana(2);

        harness.castSorceryTappingPermanents(player1, 0, player2.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot tap an already tapped creature to pay the cost")
    void cannotTapAlreadyTappedCreature() {
        Permanent alreadyTapped = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        alreadyTapped.tap();

        harness.setHand(player1, List.of(new BurnAtTheStake()));
        addMana(2);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(player1, 0, player2.getId(),
                List.of(alreadyTapped.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot tap a creature an opponent controls to pay the cost")
    void cannotTapOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new WanderingWolf());

        harness.setHand(player1, List.of(new BurnAtTheStake()));
        addMana(2);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(player1, 0, player2.getId(),
                List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot tap a non-creature permanent to pay the cost")
    void cannotTapNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ScrollOfAvacyn());

        harness.setHand(player1, List.of(new BurnAtTheStake()));
        addMana(2);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(player1, 0, player2.getId(),
                List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot count the same creature more than once for the tap cost")
    void cannotTapSameCreatureTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        harness.setHand(player1, List.of(new BurnAtTheStake()));
        addMana(2);

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(player1, 0, player2.getId(),
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        harness.assertInHand(player1, "Burn at the Stake");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can tap a summoning-sick creature and target that same creature")
    void canTapAndTargetSummoningSickCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        creature.setSummoningSick(true);
        harness.setHand(player1, List.of(new BurnAtTheStake()));
        addMana(2);

        harness.castSorceryTappingPermanents(player1, 0, creature.getId(), List.of(creature.getId()));
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wandering Wolf");
        harness.assertInGraveyard(player1, "Wandering Wolf");
    }

    @Test
    @DisplayName("Damage retains the cast-time count when a tapped creature leaves and returns untapped")
    void damageCountSurvivesFlickeringTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        harness.setHand(player1, List.of(new BurnAtTheStake(), new Cloudshift()));
        addMana(2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorceryTappingPermanents(player1, 0, player2.getId(), List.of(creature.getId()));
        assertThat(creature.isTapped()).isTrue();
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Wandering Wolf")).isNotEqualTo(creature.getId());
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Can deal damage directly to a planeswalker")
    void damagesPlaneswalker() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WanderingWolf());
        Permanent tibalt = harness.addToBattlefieldAndReturn(player2, new TibaltTheFiendBlooded());
        tibalt.setCounterCount(CounterType.LOYALTY, 2);
        harness.setHand(player1, List.of(new BurnAtTheStake()));
        addMana(2);

        harness.castSorceryTappingPermanents(player1, 0, tibalt.getId(), List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Tibalt, the Fiend-Blooded");
        harness.assertInGraveyard(player2, "Tibalt, the Fiend-Blooded");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}
