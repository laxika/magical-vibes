package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AshenMonstrosity.class, GnarledMass.class})
class AshenMonstrosityTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Ashen Monstrosity puts it on the battlefield")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new AshenMonstrosity()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Ashen Monstrosity");
    }

    @Test
    @DisplayName("Ashen Monstrosity deals 7 combat damage when unblocked")
    void dealsSevenDamageUnblocked() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new AshenMonstrosity());

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Declaring no attackers when Ashen Monstrosity can attack throws exception")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new AshenMonstrosity());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Omitting Ashen Monstrosity from attackers while declaring other creatures throws exception")
    void mustBeIncludedAmongAttackers() {
        addCreatureReady(player1, new AshenMonstrosity());
        addCreatureReady(player1, new GnarledMass());

        assertThatThrownBy(() -> declareAttackers(List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Haste lets a summoning-sick Ashen Monstrosity attack, and it must do so")
    void hasteMakesItAttackTheTurnItEnters() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new AshenMonstrosity()));

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Haste lets a summoning-sick Ashen Monstrosity deal combat damage immediately")
    void hasteAllowsImmediateAttack() {
        harness.setLife(player2, 20);

        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new AshenMonstrosity()));

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }
}
