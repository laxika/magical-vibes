package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LostOrderOfJarkeldTest extends BaseCardTest {

    @Test
    @DisplayName("Is 1/1 when the opponent controls no creatures")
    void isOneOneWithNoOpponentCreatures() {
        Permanent lostOrder = addLostOrder(player1);

        assertThat(gqs.getEffectivePower(gd, lostOrder)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lostOrder)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T equal 1 plus opponent creature count")
    void ptEqualsOnePlusOpponentCreatures() {
        Permanent lostOrder = addLostOrder(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, lostOrder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lostOrder)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not count the controller's own creatures")
    void ignoresControllerCreatures() {
        Permanent lostOrder = addLostOrder(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, lostOrder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lostOrder)).isEqualTo(2);
    }

    @Test
    @DisplayName("Updates dynamically as opponent creatures enter and leave")
    void updatesDynamically() {
        Permanent lostOrder = addLostOrder(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, lostOrder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lostOrder)).isEqualTo(2);

        harness.addToBattlefield(player2, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, lostOrder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lostOrder)).isEqualTo(3);

        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gqs.getEffectivePower(gd, lostOrder)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lostOrder)).isEqualTo(1);
    }

    @Test
    @DisplayName("CDA P/T stacks with static bonuses")
    void stacksWithStaticBonuses() {
        Permanent lostOrder = addLostOrder(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GloriousAnthem());

        assertThat(gqs.getEffectivePower(gd, lostOrder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lostOrder)).isEqualTo(3);
    }

    private Permanent addLostOrder(Player player) {
        harness.addToBattlefield(player, new LostOrderOfJarkeld());
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Lost Order of Jarkeld"))
                .findFirst()
                .orElseThrow();
    }
}
