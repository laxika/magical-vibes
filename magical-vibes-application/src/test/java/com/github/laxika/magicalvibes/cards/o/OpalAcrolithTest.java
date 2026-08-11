package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpalAcrolithTest extends BaseCardTest {

    private Permanent addOpalAcrolith() {
        return harness.addToBattlefieldAndReturn(player1, new OpalAcrolith());
    }

    private void prepareOpponentCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void castOpponentCreature() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
    }

    private void resolveOpponentCreatureAndTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("An opponent's creature spell makes Opal Acrolith a 2/4 Soldier creature")
    void becomesSoldierCreatureWhenOpponentCastsCreature() {
        Permanent opal = addOpalAcrolith();
        prepareOpponentCast();

        castOpponentCreature();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opal)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opal)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opal)).containsExactly(CardSubtype.SOLDIER);
    }

    @Test
    @DisplayName("The trigger does not fire after Opal Acrolith has become a creature")
    void doesNotTriggerWhenAlreadyCreature() {
        Permanent opal = addOpalAcrolith();
        prepareOpponentCast();

        castOpponentCreature();
        resolveOpponentCreatureAndTrigger();

        castOpponentCreature();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
    }

    @Test
    @DisplayName("The zero-mana ability restores Opal Acrolith as an enchantment")
    void becomesEnchantmentAgain() {
        Permanent opal = addOpalAcrolith();
        prepareOpponentCast();

        castOpponentCreature();
        resolveOpponentCreatureAndTrigger();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();
    }

    @Test
    @DisplayName("A noncreature spell does not trigger Opal Acrolith")
    void doesNotTriggerForNoncreatureSpell() {
        Permanent opal = addOpalAcrolith();
        prepareOpponentCast();

        harness.setHand(player2, List.of(new Spellbook()));
        harness.castArtifact(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();
    }
}
