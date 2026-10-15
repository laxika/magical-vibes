package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.j.JustTheWind;
import com.github.laxika.magicalvibes.cards.m.MagnifyingGlass;
import com.github.laxika.magicalvibes.cards.q.QuilledWolf;
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

@CardUsed({RushOfAdrenaline.class, QuilledWolf.class, MagnifyingGlass.class})
class RushOfAdrenalineTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +2/+1 and trample")
    void boostsAndGrantsTrample() {
        harness.addToBattlefield(player1, new QuilledWolf());
        harness.setHand(player1, List.of(new RushOfAdrenaline()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player1, "Quilled Wolf");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = findPermanent(player1, "Quilled Wolf");
        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(3);
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Boost and trample wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new QuilledWolf());
        harness.setHand(player1, List.of(new RushOfAdrenaline()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player1, "Quilled Wolf");
        harness.castAndResolveInstant(player1, 0, bearId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Quilled Wolf");
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new MagnifyingGlass());
        harness.setHand(player1, List.of(new RushOfAdrenaline()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID fountainId = harness.getPermanentId(player1, "Magnifying Glass");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can target an opposing creature without affecting other creatures")
    void canTargetOpponentsCreature() {
        Permanent ownWolf = harness.addToBattlefieldAndReturn(player1, new QuilledWolf());
        Permanent opposingWolf = harness.addToBattlefieldAndReturn(player2, new QuilledWolf());
        harness.setHand(player1, List.of(new RushOfAdrenaline()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, opposingWolf.getId());

        assertThat(opposingWolf.getEffectivePower()).isEqualTo(4);
        assertThat(opposingWolf.getEffectiveToughness()).isEqualTo(3);
        assertThat(opposingWolf.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(ownWolf.getEffectivePower()).isEqualTo(2);
        assertThat(ownWolf.getEffectiveToughness()).isEqualTo(2);
        assertThat(ownWolf.hasKeyword(Keyword.TRAMPLE)).isFalse();
        harness.assertInGraveyard(player1, "Rush of Adrenaline");
    }

    @Test
    @DisplayName("Repeated casts stack their boosts until cleanup")
    void repeatedCastsStackUntilEndOfTurn() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new QuilledWolf());
        harness.setHand(player1, List.of(new RushOfAdrenaline(), new RushOfAdrenaline()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, wolf.getId());
        harness.castAndResolveInstant(player1, 0, wolf.getId());

        assertThat(wolf.getEffectivePower()).isEqualTo(6);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(4);
        assertThat(wolf.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wolf.getEffectivePower()).isEqualTo(2);
        assertThat(wolf.getEffectiveToughness()).isEqualTo(2);
        assertThat(wolf.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @CardUsed({JustTheWind.class})
    @DisplayName("Does not affect another creature when its target leaves before resolution")
    void targetLeavingBeforeResolutionStopsBothEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new QuilledWolf());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new QuilledWolf());
        harness.setHand(player1, List.of(new RushOfAdrenaline()));
        harness.setHand(player2, List.of(new JustTheWind()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Quilled Wolf");
        harness.assertNotOnBattlefield(player1, "Quilled Wolf");
        harness.assertInGraveyard(player1, "Rush of Adrenaline");
        assertThat(gd.stack).isEmpty();
        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.getEffectiveToughness()).isEqualTo(2);
        assertThat(other.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }
}
