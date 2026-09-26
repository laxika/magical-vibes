package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.m.MossKami;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightOfSoulsBetrayal.class, MossKami.class, WanderingOnes.class})
class NightOfSoulsBetrayalTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures of both players get -1/-1")
    void debuffsAllCreatures() {
        harness.addToBattlefield(player1, new NightOfSoulsBetrayal());
        harness.addToBattlefield(player1, new MossKami());
        harness.addToBattlefield(player2, new MossKami());

        Permanent own = findPermanent(player1, "Moss Kami");
        Permanent opponent = findPermanent(player2, "Moss Kami");

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(4);
    }

    @Test
    @DisplayName("Debuff applies when it resolves onto the battlefield")
    void debuffAppliesOnResolve() {
        harness.addToBattlefield(player2, new MossKami());

        Permanent mossKami = findPermanent(player2, "Moss Kami");
        assertThat(gqs.getEffectivePower(gd, mossKami)).isEqualTo(5);

        harness.castFromHand(player1, new NightOfSoulsBetrayal(), "{2}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Night of Souls' Betrayal");
        assertThat(gqs.getEffectivePower(gd, mossKami)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mossKami)).isEqualTo(4);
    }

    @Test
    @DisplayName("Two copies stack to -2/-2")
    void twoCopiesStack() {
        harness.addToBattlefield(player1, new NightOfSoulsBetrayal());
        harness.addToBattlefield(player2, new NightOfSoulsBetrayal());
        harness.addToBattlefield(player1, new MossKami());

        Permanent mossKami = findPermanent(player1, "Moss Kami");

        assertThat(gqs.getEffectivePower(gd, mossKami)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mossKami)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures with toughness 1 die to state-based actions")
    void oneToughnessCreaturesDie() {
        harness.addToBattlefield(player1, new NightOfSoulsBetrayal());
        harness.addToBattlefield(player1, new WanderingOnes());
        harness.addToBattlefield(player2, new WanderingOnes());

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Wandering Ones");
        harness.assertNotOnBattlefield(player2, "Wandering Ones");
    }

    @Test
    @DisplayName("Debuff is removed when it leaves the battlefield")
    void debuffRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new NightOfSoulsBetrayal());
        harness.addToBattlefield(player1, new MossKami());

        Permanent mossKami = findPermanent(player1, "Moss Kami");
        assertThat(gqs.getEffectivePower(gd, mossKami)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Night of Souls' Betrayal"));

        assertThat(gqs.getEffectivePower(gd, mossKami)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mossKami)).isEqualTo(5);
    }

    @Test
    @CardUsed(Opalescence.class)
    @DisplayName("All creatures includes Night of Souls' Betrayal when it becomes a creature")
    void debuffAppliesToAnimatedSource() {
        Permanent night = harness.addToBattlefieldAndReturn(player1, new NightOfSoulsBetrayal());
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, night)).isTrue();
        assertThat(gqs.getEffectivePower(gd, night)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, night)).isEqualTo(3);
    }
}
