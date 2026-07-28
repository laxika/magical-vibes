package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SunstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2} and sacrificing a snow land prevents all combat damage this turn")
    void preventsAllCombatDamage() {
        addSunstone(player1);
        Permanent snowLand = addLand(player1, true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.preventAllCombatDamage).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(snowLand);
    }

    @Test
    @DisplayName("An unblocked attacker deals no combat damage after the ability resolves")
    void unblockedAttackerDealsNoDamage() {
        harness.setLife(player1, 20);
        addSunstone(player1);
        addLand(player1, true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot activate with only a nonsnow land")
    void cannotActivateWithoutSnowLand() {
        addSunstone(player1);
        addLand(player1, false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addSunstone(Player player) {
        gd.playerBattlefields.get(player.getId()).add(new Permanent(new Sunstone()));
    }

    private Permanent addLand(Player player, boolean snow) {
        Permanent land = new Permanent(new Mountain());
        if (snow) {
            TestCards.mutableCard(land).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        }
        gd.playerBattlefields.get(player.getId()).add(land);
        return land;
    }
}
