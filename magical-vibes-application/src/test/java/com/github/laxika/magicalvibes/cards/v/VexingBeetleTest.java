package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VexingBeetle.class, Cancel.class, GrizzlyBears.class})
class VexingBeetleTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +3/+3 while no opponent controls a creature")
    void getsBoostWhileNoOpponentControlsCreature() {
        harness.addToBattlefield(player1, new VexingBeetle());

        assertStats(6, 6);
    }

    @Test
    @DisplayName("Loses +3/+3 while an opponent controls a creature")
    void losesBoostWhileOpponentControlsCreature() {
        harness.addToBattlefield(player1, new VexingBeetle());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertStats(3, 3);

        gd.playerBattlefields.get(player2.getId()).remove(opponentCreature);

        assertStats(6, 6);
    }

    @Test
    @DisplayName("The spell can't be countered")
    void spellCannotBeCountered() {
        VexingBeetle beetle = new VexingBeetle();
        harness.setHand(player1, List.of(beetle));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, beetle.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vexing Beetle");
        harness.assertInGraveyard(player2, "Cancel");
    }

    private void assertStats(int power, int toughness) {
        Permanent beetle = findPermanent(player1, "Vexing Beetle");
        assertThat(gqs.getEffectivePower(gd, beetle)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, beetle)).isEqualTo(toughness);
    }
}
