package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MerfolkSpy;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({SingerOfSwiftRivers.class, GrizzlyBears.class, MerfolkSpy.class})
class SingerOfSwiftRiversTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a shield counter on another creature you control")
    void etbPutsShieldCounterOnAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SingerOfSwiftRivers()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB cannot target a creature an opponent controls")
    void etbCannotTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SingerOfSwiftRivers()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature you control");
    }

    @Test
    @DisplayName("Controller can cast Merfolk spells at instant speed")
    void controllerCanCastMerfolkAtInstantSpeed() {
        harness.addToBattlefield(player1, new SingerOfSwiftRivers());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new MerfolkSpy(), "{U}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Controller cannot cast non-Merfolk creatures at instant speed")
    void controllerCannotCastNonMerfolkAtInstantSpeed() {
        harness.addToBattlefield(player1, new SingerOfSwiftRivers());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Singer itself can be cast at instant speed with no other creatures")
    void singerCanBeCastWithoutAShieldTarget() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new SingerOfSwiftRivers(), "{1}{G}{U}");
        resolveAllTriggers();

        Permanent singer = findPermanent(player1, "Singer of Swift Rivers");
        assertThat(singer.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Another Singer is a legal shield-counter target")
    void anotherSingerCanReceiveShieldCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SingerOfSwiftRivers());
        harness.setHand(player1, List.of(new SingerOfSwiftRivers()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(findPermanents(player1, "Singer of Swift Rivers"))
                .filteredOn(permanent -> !permanent.getId().equals(target.getId()))
                .allSatisfy(permanent -> assertThat(permanent.getCounterCount(CounterType.SHIELD)).isZero());
    }

    @Test
    @DisplayName("Singer does not grant flash timing to an opponent's Merfolk")
    void opponentsMerfolkDoNotGainFlashTiming() {
        harness.addToBattlefield(player1, new SingerOfSwiftRivers());
        harness.setHand(player2, List.of(new MerfolkSpy()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Flash timing ends when Singer leaves the battlefield")
    void merfolkLoseFlashTimingWhenSingerLeaves() {
        Permanent singer = harness.addToBattlefieldAndReturn(player1, new SingerOfSwiftRivers());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MerfolkSpy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        gd.playerBattlefields.get(player1.getId()).remove(singer);
        gd.playerGraveyards.get(player1.getId()).add(singer.getCard());
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
