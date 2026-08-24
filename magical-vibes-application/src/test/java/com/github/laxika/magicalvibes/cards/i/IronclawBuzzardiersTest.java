package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IronclawBuzzardiers.class, FugitiveWizard.class, GrizzlyBears.class, HillGiant.class})
class IronclawBuzzardiersTest extends BaseCardTest {

    @Test
    @DisplayName("Can block an attacker with power 1")
    void canBlockPowerOne() {
        Permanent buzzardiers = addReadyBuzzardiers(player2);
        Permanent attacker = addAttacker(new FugitiveWizard());

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(indexOf(player2, buzzardiers), indexOf(player1, attacker)))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Cannot block an attacker with power 2")
    void cannotBlockPowerTwo() {
        Permanent buzzardiers = addReadyBuzzardiers(player2);
        Permanent attacker = addAttacker(new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(indexOf(player2, buzzardiers), indexOf(player1, attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too high");
    }

    @Test
    @DisplayName("Cannot block an attacker with power greater than 2")
    void cannotBlockPowerGreaterThanTwo() {
        Permanent buzzardiers = addReadyBuzzardiers(player2);
        Permanent attacker = addAttacker(new HillGiant());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(indexOf(player2, buzzardiers), indexOf(player1, attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too high");
    }

    @Test
    @DisplayName("Red activation grants flying until end of turn")
    void grantsFlyingUntilEndOfTurn() {
        Permanent buzzardiers = addReadyBuzzardiers(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, buzzardiers, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, buzzardiers, Keyword.FLYING)).isFalse();
    }

    private Permanent addReadyBuzzardiers(Player player) {
        return addCreatureReady(player, new IronclawBuzzardiers());
    }

    private Permanent addAttacker(Card card) {
        Permanent attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
        return attacker;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
