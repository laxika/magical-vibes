package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NightOfSoulsBetrayalTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures of both players get -1/-1")
    void debuffsAllCreatures() {
        harness.addToBattlefield(player1, new NightOfSoulsBetrayal());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent own = findPermanent(player1, "Grizzly Bears");
        Permanent opponent = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(1);
    }

    @Test
    @DisplayName("Debuff applies when it resolves onto the battlefield")
    void debuffAppliesOnResolve() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NightOfSoulsBetrayal()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Night of Souls' Betrayal");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two copies stack to -2/-2")
    void twoCopiesStack() {
        harness.addToBattlefield(player1, new NightOfSoulsBetrayal());
        harness.addToBattlefield(player2, new NightOfSoulsBetrayal());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, bears)).isZero();
    }

    @Test
    @DisplayName("Creatures with toughness 1 die to state-based actions")
    void oneToughnessCreaturesDie() {
        harness.addToBattlefield(player1, new NightOfSoulsBetrayal());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new LlanowarElves());

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Debuff is removed when it leaves the battlefield")
    void debuffRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new NightOfSoulsBetrayal());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Night of Souls' Betrayal"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
