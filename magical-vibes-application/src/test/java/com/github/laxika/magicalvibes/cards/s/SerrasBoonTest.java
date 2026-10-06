package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenRiftwatcher;
import com.github.laxika.magicalvibes.cards.g.GaeasAnthem;
import com.github.laxika.magicalvibes.cards.n.NeedlepeakSpider;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerrasBoon.class, AvenRiftwatcher.class, NeedlepeakSpider.class, GaeasAnthem.class})
class SerrasBoonTest extends BaseCardTest {

    @Test
    @DisplayName("White enchanted creature gets +1/+2")
    void whiteCreatureGetsBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AvenRiftwatcher());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SerrasBoon());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Nonwhite enchanted creature gets -2/-1")
    void nonWhiteCreatureGetsDebuff() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NeedlepeakSpider());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SerrasBoon());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Serra's Boon stops affecting the creature when it leaves the battlefield")
    void effectStopsWhenRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NeedlepeakSpider());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SerrasBoon());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Serra's Boon resolves attached to a target creature")
    void resolvesAttachedToTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AvenRiftwatcher());
        harness.setHand(player1, List.of(new SerrasBoon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof SerrasBoon
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(creature.getId()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Serra's Boon")
    void cannotTargetNonCreature() {
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player1, new GaeasAnthem());
        harness.setHand(player1, List.of(new SerrasBoon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Serra's Boon fizzles if its target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NeedlepeakSpider());

        harness.setHand(player1, List.of(new SerrasBoon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Serra's Boon");
        harness.assertNotOnBattlefield(player1, "Serra's Boon");
    }

    @Test
    @DisplayName("A creature that gains white gets the boost even while it remains red")
    void switchesBranchesWhenCreatureGainsAndLosesWhite() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NeedlepeakSpider());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SerrasBoon());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);

        creature.getGrantedColors().add(CardColor.WHITE);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        creature.getGrantedColors().clear();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("A naturally white creature gets the penalty when its color is replaced")
    void usesCurrentColorInsteadOfPrintedColor() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AvenRiftwatcher());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SerrasBoon());
        aura.setAttachedTo(creature.getId());

        creature.setColorOverridden(true);
        creature.getTransientColors().add(CardColor.RED);

        assertThat(gqs.getEffectivePower(gd, creature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        creature.setColorOverridden(false);
        creature.getTransientColors().clear();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Serra's Boon can enchant and penalize an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NeedlepeakSpider());
        harness.setHand(player1, List.of(new SerrasBoon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Serra's Boon");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }
}
