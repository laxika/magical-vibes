package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChainOfVapor;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.Gigapede;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({BlatantThievery.class, ElvishWarrior.class, Gigapede.class, Island.class, ChainOfVapor.class})
class BlatantThieveryTest extends BaseCardTest {

    @Test
    @DisplayName("Gains permanent control of one target permanent from the opponent")
    void gainsPermanentControlOfTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());

        cast(target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Cannot be cast when the opponent controls no permanent")
    void cannotCastWithNoOpposingPermanent() {
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be cast when the opponent's only permanent has shroud")
    void cannotCastWhenOpponentHasNoLegalTarget() {
        harness.addToBattlefield(player2, new Gigapede());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can take a land without untapping it")
    void gainsControlOfTappedLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        land.tap();

        cast(land.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(land);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not take a replacement target when its target leaves the battlefield")
    void doesNotRetargetWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        prepareCast();
        harness.castSorcery(player1, 0, target.getId());

        harness.setHand(player2, List.of(new ChainOfVapor()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertInHand(player2, "Elvish Warrior");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target, other);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(other).doesNotContain(target);
        harness.assertInGraveyard(player1, "Blatant Thievery");
    }

    @Test
    @DisplayName("Requires a target when the opponent controls a permanent")
    void requiresTargetWhenOpponentControlsPermanent() {
        harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by its caster")
    void cannotTargetOwnPermanent() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, ownPermanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose two permanents controlled by the same opponent")
    void cannotChooseTwoPermanentsFromSameOpponent() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Control persists after the turn ends")
    void controlDoesNotExpireAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());

        cast(target.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    private void cast(UUID targetId) {
        prepareCast();
        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new BlatantThievery()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
