package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KarfellKennelMaster.class, GrizzlyBears.class, Island.class})
class KarfellKennelMasterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives up to two target creatures +1/+0 and indestructible")
    void boostsAndProtectsTwoTargetCreatures() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castKarfellKennelMaster(List.of(ownBear.getId(), opposingBear.getId()));

        assertThat(ownBear.getEffectivePower()).isEqualTo(3);
        assertThat(opposingBear.getEffectivePower()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("ETB can choose no target creatures")
    void canChooseNoTargets() {
        castKarfellKennelMaster(List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof KarfellKennelMaster);
    }

    @Test
    @DisplayName("ETB effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castKarfellKennelMaster(List.of(bear.getId()));

        assertThat(bear.getEffectivePower()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("ETB cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new KarfellKennelMaster()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseMoreThanTwoTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new KarfellKennelMaster());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new KarfellKennelMaster());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new KarfellKennelMaster());
        harness.setHand(player1, List.of(new KarfellKennelMaster()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseTheSameCreatureTwice() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KarfellKennelMaster());
        harness.setHand(player1, List.of(new KarfellKennelMaster()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void remainingTargetGetsBothEffectsWhenOtherTargetLeaves() {
        Permanent remaining = harness.addToBattlefieldAndReturn(player1, new KarfellKennelMaster());
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new KarfellKennelMaster());
        Permanent untargeted = harness.addToBattlefieldAndReturn(player1, new KarfellKennelMaster());
        harness.setHand(player1, List.of(new KarfellKennelMaster()));
        addMana();
        harness.castCreature(player1, 0, List.of(remaining.getId(), removed.getId()));
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(removed);
        harness.setGraveyard(player2, List.of(removed.getCard()));
        harness.passBothPriorities();

        assertThat(remaining.getEffectivePower()).isEqualTo(5);
        assertThat(remaining.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, remaining, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(untargeted.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, untargeted, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void castKarfellKennelMaster(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new KarfellKennelMaster()));
        addMana();

        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
