package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DawnhartGeist.class, GhostlyPrison.class, GrizzlyBears.class})
class DawnhartGeistTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life whenever you cast an enchantment spell")
    void gainsLifeOnEnchantmentCast() {
        harness.addToBattlefield(player1, new DawnhartGeist());
        harness.setHand(player1, List.of(new GhostlyPrison()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int startingLife = gd.getLife(player1.getId());

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife + 2);
    }

    @Test
    @DisplayName("Does not trigger for a creature spell")
    void noLifeOnCreatureCast() {
        harness.addToBattlefield(player1, new DawnhartGeist());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int startingLife = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's enchantment spell")
    void noLifeOnOpponentEnchantmentCast() {
        harness.addToBattlefield(player1, new DawnhartGeist());
        harness.setHand(player2, List.of(new GhostlyPrison()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        int startingLife = gd.getLife(player1.getId());

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
    }
}
