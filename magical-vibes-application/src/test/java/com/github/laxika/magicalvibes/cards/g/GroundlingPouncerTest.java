package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GroundlingPouncerTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate when no opponent controls a creature with flying")
    void cannotActivateWithoutOpponentFlyer() {
        addReadyPouncer(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls a creature with flying");
    }

    @Test
    @DisplayName("Resolving gives +1/+3 and flying when an opponent controls a flyer")
    void resolvingBoostsAndGrantsFlying() {
        Permanent pouncer = addReadyPouncer(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addToBattlefield(player2, new SuntailHawk());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, pouncer)).isEqualTo(3);   // 2 + 1
        assertThat(gqs.getEffectiveToughness(gd, pouncer)).isEqualTo(4); // 1 + 3
        assertThat(gqs.hasKeyword(gd, pouncer, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Can only be activated once each turn")
    void onlyOncePerTurn() {
        addReadyPouncer(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addToBattlefield(player2, new SuntailHawk());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 1 times each turn");
    }

    @Test
    @DisplayName("Boost and flying wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent pouncer = addReadyPouncer(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addToBattlefield(player2, new SuntailHawk());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pouncer, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, pouncer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, pouncer)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, pouncer, Keyword.FLYING)).isFalse();
    }

    private Permanent addReadyPouncer(Player player) {
        Permanent perm = new Permanent(new GroundlingPouncer());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
