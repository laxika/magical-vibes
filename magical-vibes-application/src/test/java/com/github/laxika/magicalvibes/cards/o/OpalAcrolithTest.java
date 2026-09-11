package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Fluctuator;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.p.PouncingCheetah;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OpalAcrolith.class, GorillaWarrior.class, Fluctuator.class, PouncingCheetah.class})
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
        harness.setHand(player2, List.of(new GorillaWarrior()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castCreature(player2, 0);
    }

    private void castOpponentFlashCreature() {
        harness.setHand(player2, List.of(new PouncingCheetah()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castCreature(player2, 0);
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
        resolveAllTriggers();

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
        resolveAllTriggers();

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

        harness.setHand(player2, List.of(new Fluctuator()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castArtifact(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();
    }

    @Test
    @DisplayName("Opal Acrolith does not trigger for its controller's creature spell")
    void doesNotTriggerForControllerCreatureSpell() {
        Permanent opal = addOpalAcrolith();

        harness.setHand(player1, List.of(new GorillaWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();
    }

    @Test
    @DisplayName("A queued trigger does nothing if Opal Acrolith is no longer an enchantment when it resolves")
    void queuedTriggerChecksEnchantmentAgainAtResolution() {
        Permanent opal = addOpalAcrolith();
        prepareOpponentCast();

        castOpponentCreature();
        castOpponentFlashCreature();
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
        assertThat(gd.gameLog.stream()
                .map(entry -> entry.plainText())
                .filter(log -> log.contains("becomes a 2/4 creature")))
                .hasSize(1);
    }
}
