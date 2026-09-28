package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(IntrepidAce.class)
class IntrepidAceTest extends BaseCardTest {

    @Test
    @DisplayName("Intrepid Ace gets +2/+0 while it is neither attacking nor blocking")
    void boostsWhileNotAttackingOrBlocking() {
        Permanent ace = addCreatureReady(player1, new IntrepidAce());

        assertThat(gqs.getEffectivePower(gd, ace)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ace)).isEqualTo(1);
    }

    @Test
    @DisplayName("Intrepid Ace loses its boost while attacking")
    void losesBoostWhileAttacking() {
        Permanent ace = addCreatureReady(player1, new IntrepidAce());
        ace.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, ace)).isEqualTo(2);
    }

    @Test
    @DisplayName("Intrepid Ace loses its boost while blocking")
    void losesBoostWhileBlocking() {
        Permanent ace = addCreatureReady(player1, new IntrepidAce());
        ace.setBlocking(true);

        assertThat(gqs.getEffectivePower(gd, ace)).isEqualTo(2);
    }

    @Test
    @DisplayName("Intrepid Ace does not get its boost while both attacking and blocking")
    void losesBoostWhileBothAttackingAndBlocking() {
        Permanent ace = addCreatureReady(player1, new IntrepidAce());
        ace.setAttacking(true);
        ace.setBlocking(true);

        assertThat(gqs.getEffectivePower(gd, ace)).isEqualTo(2);
    }
}
