package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({DauntlessOnslaught.class, TravelingPhilosopher.class, Mountain.class})
class DauntlessOnslaughtTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Both target creatures get +2/+2")
    void twoTargetsGetBoosted() {
        Permanent a = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        Permanent b = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new DauntlessOnslaught()));
        giveMana();

        harness.castAndResolveInstant(player1, 0, List.of(a.getId(), b.getId()));

        assertThat(gqs.getEffectivePower(gd, a)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, a)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, b)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, b)).isEqualTo(4);
    }

    @Test
    @DisplayName("May target only one creature")
    void singleTargetAllowed() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new DauntlessOnslaught()));
        giveMana();

        harness.castAndResolveInstant(player1, 0, List.of(bears.getId()));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void wearsOff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new DauntlessOnslaught()));
        giveMana();

        harness.castAndResolveInstant(player1, 0, List.of(bears.getId()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void zeroTargetsAllowed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new DauntlessOnslaught()));
        giveMana();

        harness.castAndResolveInstant(player1, 0, List.<UUID>of());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Dauntless Onslaught");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void canBoostBothPlayersCreatures() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new DauntlessOnslaught()));
        giveMana();

        harness.castAndResolveInstant(player1, 0, List.of(own.getId(), opposing.getId()));

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(4);
    }

    @Test
    void remainingLegalTargetStillGetsBoost() {
        Permanent a = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        Permanent b = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new DauntlessOnslaught()));
        giveMana();

        harness.castInstant(player1, 0, List.of(a.getId(), b.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(a);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, b)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, b)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Dauntless Onslaught");
    }

    @Test
    void allTargetsRemovedSpellDoesNotResolve() {
        Permanent a = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        Permanent b = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new DauntlessOnslaught()));
        giveMana();

        harness.castInstant(player1, 0, List.of(a.getId(), b.getId()));
        gd.playerBattlefields.get(player1.getId()).removeAll(List.of(a, b));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Dauntless Onslaught");
        assertThat(a.getPowerModifier()).isZero();
        assertThat(a.getToughnessModifier()).isZero();
        assertThat(b.getPowerModifier()).isZero();
        assertThat(b.getToughnessModifier()).isZero();
    }

    @Test
    void targetGainingHexproofIsNotBoosted() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new DauntlessOnslaught()));
        giveMana();

        harness.castInstant(player1, 0, List.of(own.getId(), opposing.getId()));
        opposing.getGrantedKeywords().add(Keyword.HEXPROOF);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(2);
    }

    @Test
    void cannotChooseSameCreatureTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new DauntlessOnslaught()));
        giveMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseThreeCreatures() {
        Permanent a = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        Permanent b = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        Permanent c = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new DauntlessOnslaught()));
        giveMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(a.getId(), b.getId(), c.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new DauntlessOnslaught()));
        giveMana();

        UUID mountainId = mountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(mountainId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
