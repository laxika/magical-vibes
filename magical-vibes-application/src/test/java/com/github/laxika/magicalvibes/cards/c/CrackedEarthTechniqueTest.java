package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrackedEarthTechnique.class, Forest.class})
class CrackedEarthTechniqueTest extends BaseCardTest {

    @Test
    void earthbendsTheSameLandTwiceAndGainsThreeLife() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new CrackedEarthTechnique()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(land.getId(), land.getId()));

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    void cannotEarthbendALandControlledByOpponent() {
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new CrackedEarthTechnique()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(opponentLand.getId(), opponentLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void earthbendsTwoDifferentLands() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new CrackedEarthTechnique()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));

        for (Permanent land : List.of(first, second)) {
            assertThat(gqs.isCreature(gd, land)).isTrue();
            assertThat(gqs.isLand(gd, land)).isTrue();
            assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
            assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        }
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    void twiceEarthbentLandReturnsTappedWithoutAnimationAfterDying() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new CrackedEarthTechnique()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, List.of(land.getId(), land.getId()));

        land.setMarkedDamage(6);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Forest");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent returned = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isLand(gd, returned)).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    void twiceEarthbentLandReturnsTappedAfterExile() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new CrackedEarthTechnique()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, List.of(land.getId(), land.getId()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, land));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent returned = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.findExiledCard(land.getCard().getId())).isNull();
    }

    @Test
    void gainsNoLifeWhenBothTargetOccurrencesBecomeIllegal() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new CrackedEarthTechnique()));
        addMana();
        harness.castSorcery(player1, 0, List.of(land.getId(), land.getId()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, land));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Cracked Earth Technique");
        harness.assertInHand(player1, "Forest");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
