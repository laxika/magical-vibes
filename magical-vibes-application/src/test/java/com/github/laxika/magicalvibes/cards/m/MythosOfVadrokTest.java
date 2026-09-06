package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MythosOfVadrok.class, HillGiant.class, ChandraNalaar.class})
class MythosOfVadrokTest extends BaseCardTest {

    @Test
    @DisplayName("Deals five damage divided among target creatures")
    void dealsFiveDamageDividedAmongCreatures() {
        Permanent lightlyDamaged = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent lethallyDamaged = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castWithMana(Map.of(lightlyDamaged.getId(), 2, lethallyDamaged.getId(), 3), false);

        assertThat(lightlyDamaged.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(lethallyDamaged.getId()));
    }

    @Test
    @DisplayName("Spending white and blue prevents a surviving creature from attacking until the caster's next turn")
    void enhancedModeLocksCreatureFromAttacking() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        target.setSummoningSick(false);
        Permanent other = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castWithMana(Map.of(target.getId(), 1, other.getId(), 4), true);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        gd.expireFloatingEffectsAtTurnStart(player1.getId());
        declareAttackers(player2, List.of(0));
    }

    @Test
    @DisplayName("Can target a planeswalker and lock its activated abilities")
    void enhancedModeTargetsAndLocksPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        castWithMana(Map.of(planeswalker.getId(), 5), true);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        int planeswalkerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(planeswalker);
        assertThatThrownBy(() -> harness.activateAbility(player2, planeswalkerIndex, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    private void castWithMana(Map<java.util.UUID, Integer> assignments, boolean enhanced) {
        harness.setHand(player1, List.of(new MythosOfVadrok()));
        harness.addMana(player1, ManaColor.RED, 2);
        if (enhanced) {
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.addMana(player1, ManaColor.BLUE, 1);
        } else {
            harness.addMana(player1, ManaColor.COLORLESS, 2);
        }
        harness.castSorcery(player1, 0, assignments);
        harness.passBothPriorities();
    }
}
