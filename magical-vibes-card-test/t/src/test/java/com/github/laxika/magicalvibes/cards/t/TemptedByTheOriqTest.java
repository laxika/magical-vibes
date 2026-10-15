package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.p.PrismariCampus;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemptedByTheOriq.class, GrizzlyBears.class, HillGiant.class, JaceBeleren.class,
        PrismariCampus.class, TrollAscetic.class, Unsummon.class})
class TemptedByTheOriqTest extends BaseCardTest {

    @Test
    @DisplayName("Gains permanent control of an eligible creature")
    void gainsPermanentControlOfCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castTemptedByTheOriq(List.of(bear.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        assertThat(gd.isStolenUntilEndOfTurn(bear.getId())).isFalse();
    }

    @Test
    @DisplayName("Can gain permanent control of an eligible planeswalker")
    void gainsPermanentControlOfPlaneswalker() {
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);

        castTemptedByTheOriq(List.of(jace.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(jace);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(jace);
    }

    @Test
    @DisplayName("Can choose no targets")
    void canChooseNoTargets() {
        castTemptedByTheOriq(List.of());

        harness.assertInGraveyard(player1, "Tempted by the Oriq");
    }

    @Test
    @DisplayName("Cannot target a permanent with mana value greater than three")
    void cannotTargetPermanentWithManaValueGreaterThanThree() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        assertThatThrownBy(() -> castTemptedByTheOriq(List.of(giant.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target your own creature")
    void cannotTargetOwnCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> castTemptedByTheOriq(List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose two permanents controlled by the same opponent")
    void cannotChooseTwoPermanentsControlledBySameOpponent() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> castTemptedByTheOriq(List.of(firstBear.getId(), secondBear.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    @Test
    @DisplayName("Can decline to target even when an eligible creature exists")
    void canDeclineEligibleTarget() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castTemptedByTheOriq(List.of());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
        harness.assertInGraveyard(player1, "Tempted by the Oriq");
    }

    @Test
    @DisplayName("Cannot target a land even though its mana value is zero")
    void cannotTargetLand() {
        Permanent campus = harness.addToBattlefieldAndReturn(player2, new PrismariCampus());

        assertThatThrownBy(() -> castTemptedByTheOriq(List.of(campus.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opposing creature with hexproof")
    void cannotTargetHexproofCreature() {
        Permanent troll = harness.addToBattlefieldAndReturn(player2, new TrollAscetic());

        assertThatThrownBy(() -> castTemptedByTheOriq(List.of(troll.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gaining control does not untap the creature or grant haste")
    void doesNotUntapOrGrantHaste() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bear.tap();
        bear.setSummoningSick(false);

        castTemptedByTheOriq(List.of(bear.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Control remains after the casting turn ends")
    void controlRemainsAfterEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castTemptedByTheOriq(List.of(bear.getId()));
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
    }

    @Test
    @DisplayName("Does not gain control when the target returns to hand before resolution")
    void targetLeavesBattlefieldBeforeResolution() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareTemptedByTheOriq();
        harness.castSorcery(player1, 0, List.of(bear.getId()));

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, bear.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).contains(bear.getCard());
        harness.assertInGraveyard(player1, "Tempted by the Oriq");
        assertThat(gd.stack).isEmpty();
    }

    private void prepareTemptedByTheOriq() {
        harness.setHand(player1, List.of(new TemptedByTheOriq()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castTemptedByTheOriq(List<UUID> targetIds) {
        prepareTemptedByTheOriq();
        harness.castAndResolveSorcery(player1, 0, targetIds);
    }
}
