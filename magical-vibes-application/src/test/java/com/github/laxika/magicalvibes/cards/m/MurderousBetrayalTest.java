package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MurderousBetrayalTest extends BaseCardTest {

    private Permanent betrayal(int life) {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new MurderousBetrayal());
        harness.setLife(player1, life);
        harness.addMana(player1, ManaColor.BLACK, 2);
        return enchantment;
    }

    @Test
    @DisplayName("Destroys target nonblack creature and pays half life rounded up")
    void destroysNonblackCreature() {
        Permanent enchantment = betrayal(20);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(enchantment);
        harness.activateAbility(player1, idx, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Half-life cost rounds up on odd life totals")
    void roundsUpOddLife() {
        Permanent enchantment = betrayal(21);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(enchantment);
        harness.activateAbility(player1, idx, null, bears.getId());
        harness.passBothPriorities();

        // 21 / 2 rounded up = 11 paid, 10 remaining
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent enchantment = betrayal(20);
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new ScatheZombies());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(enchantment);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, zombie.getId()))
                .isInstanceOf(IllegalStateException.class);

        // Life untouched because the illegal activation rewinds with no cost paid.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Scathe Zombies"));
    }
}
