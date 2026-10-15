package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BeastWalkers;
import com.github.laxika.magicalvibes.cards.j.Jump;
import com.github.laxika.magicalvibes.cards.w.WillowFaerie;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerraAviary.class, WillowFaerie.class, BeastWalkers.class, Jump.class})
class SerraAviaryTest extends BaseCardTest {

    @Test
    @DisplayName("The bonus follows temporarily granted flying and ends when flying expires")
    void bonusFollowsTemporaryFlying() {
        harness.addToBattlefield(player1, new SerraAviary());
        Permanent walkers = harness.addToBattlefieldAndReturn(player2, new BeastWalkers());
        harness.setHand(player1, List.of(new Jump()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThat(gqs.getEffectivePower(gd, walkers)).isEqualTo(2);
        harness.castAndResolveInstant(player1, 0, walkers.getId());

        assertThat(gqs.getEffectivePower(gd, walkers)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, walkers)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, walkers, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, walkers)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, walkers)).isEqualTo(2);
    }

    @Test
    @DisplayName("A newer Serra Aviary replaces an opponent's older world enchantment")
    void newerWorldReplacesOlderAviary() {
        harness.enterBattlefieldAndReturn(player1, new SerraAviary());
        Permanent flier = harness.addToBattlefieldAndReturn(player1, new WillowFaerie());
        harness.enterBattlefieldAndReturn(player2, new SerraAviary());

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Serra Aviary");
        harness.assertInGraveyard(player1, "Serra Aviary");
        harness.assertOnBattlefield(player2, "Serra Aviary");
        assertThat(gqs.getEffectivePower(gd, flier)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, flier)).isEqualTo(3);
    }

    @Test
    @DisplayName("Own creatures with flying get +1/+1")
    void buffsOwnFliers() {
        harness.addToBattlefield(player1, new SerraAviary());
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new WillowFaerie());

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent creatures with flying also get +1/+1")
    void buffsOpponentFliers() {
        harness.addToBattlefield(player1, new SerraAviary());
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new WillowFaerie());

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures without flying are unaffected")
    void doesNotBuffGroundCreatures() {
        harness.addToBattlefield(player1, new SerraAviary());
        Permanent walkers = harness.addToBattlefieldAndReturn(player1, new BeastWalkers());

        assertThat(gqs.getEffectivePower(gd, walkers)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, walkers)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bonus is removed when Serra Aviary leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent aviary = harness.addToBattlefieldAndReturn(player1, new SerraAviary());
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new WillowFaerie());
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(aviary);

        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(2);
    }

    @Test
    @DisplayName("A flying creature version of Serra Aviary buffs itself")
    void buffsItselfWhenItBecomesAFlyingCreature() {
        SerraAviary card = new SerraAviary();
        card.setType(CardType.CREATURE);
        card.setAdditionalTypes(Set.of(CardType.ENCHANTMENT));
        card.setPower(4);
        card.setToughness(4);
        card.setKeywords(Set.of(Keyword.FLYING));
        Permanent aviary = harness.addToBattlefieldAndReturn(player1, card);

        assertThat(gqs.hasKeyword(gd, aviary, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, aviary)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, aviary)).isEqualTo(5);
    }
}
