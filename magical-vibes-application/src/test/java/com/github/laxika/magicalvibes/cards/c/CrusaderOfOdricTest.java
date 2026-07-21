package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CrusaderOfOdricTest extends BaseCardTest {

    @Test
    @DisplayName("Crusader of Odric is 1/1 when it is your only creature")
    void isOneOneWhenOnlyCreature() {
        Permanent crusader = addCrusaderReady(player1);

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(1);
    }

    @Test
    @DisplayName("Crusader of Odric power and toughness equal creatures you control")
    void ptEqualsControlledCreatures() {
        Permanent crusader = addCrusaderReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(3);
    }

    @Test
    @DisplayName("Crusader of Odric counts only your creatures, not opponent creatures")
    void countsOnlyControllersCreatures() {
        Permanent crusader = addCrusaderReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(1);
    }

    @Test
    @DisplayName("Crusader of Odric power and toughness update as creatures enter and leave")
    void ptUpdatesAsCreaturesChange() {
        Permanent crusader = addCrusaderReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(2);

        harness.addToBattlefield(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(1);
    }

    @Test
    @DisplayName("Crusader of Odric characteristic-defining P/T stacks with static bonuses")
    void ptStacksWithStaticBonuses() {
        Permanent crusader = addCrusaderReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GloriousAnthem());

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(3);
    }

    private Permanent addCrusaderReady(Player player) {
        CrusaderOfOdric card = new CrusaderOfOdric();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
