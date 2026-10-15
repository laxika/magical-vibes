package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.d.DaybreakRanger;
import com.github.laxika.magicalvibes.cards.h.HamletCaptain;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ButchersCleaver.class, HamletCaptain.class, WalkingCorpse.class, DaybreakRanger.class})
class ButchersCleaverTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+0 regardless of creature type")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent cleaver = harness.addToBattlefieldAndReturn(player1, new ButchersCleaver());
        cleaver.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);   // 2 + 3
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2); // 2 + 0
    }

    @Test
    @DisplayName("Equipped Human creature gets +3/+0")
    void equippedHumanGetsBoost() {
        Permanent human = addCreatureReady(player1, new HamletCaptain());
        Permanent cleaver = harness.addToBattlefieldAndReturn(player1, new ButchersCleaver());
        cleaver.setAttachedTo(human.getId());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(5);   // 2 + 3
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(2); // 2 + 0
    }

    @Test
    @DisplayName("Equipped Human creature has lifelink")
    void equippedHumanHasLifelink() {
        Permanent human = addCreatureReady(player1, new HamletCaptain());
        Permanent cleaver = harness.addToBattlefieldAndReturn(player1, new ButchersCleaver());
        cleaver.setAttachedTo(human.getId());

        assertThat(gqs.hasKeyword(gd, human, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Equipped non-Human creature does not have lifelink")
    void equippedNonHumanDoesNotHaveLifelink() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent cleaver = harness.addToBattlefieldAndReturn(player1, new ButchersCleaver());
        cleaver.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Controller gains life when equipped Human deals combat damage")
    void lifelinkGainsLifeOnHumanCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent human = addCreatureReady(player1, new HamletCaptain());
        Permanent cleaver = harness.addToBattlefieldAndReturn(player1, new ButchersCleaver());
        cleaver.setAttachedTo(human.getId());
        human.setAttacking(true);

        resolveCombat();

        // Human has 5 power (2 + 3), player2 takes 5: 20 - 5 = 15
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        // Player1 gains 5 from lifelink: 20 + 5 = 25
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
    }

    @Test
    @DisplayName("Controller does not gain life when equipped non-Human deals combat damage")
    void noLifelinkOnNonHumanCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent cleaver = harness.addToBattlefieldAndReturn(player1, new ButchersCleaver());
        cleaver.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        // Creature has 5 power (2 + 3), player2 takes 5: 20 - 5 = 15
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        // Player1 does NOT gain life (no lifelink): stays at 20
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Moving Cleaver from Human to non-Human removes lifelink")
    void movingFromHumanToNonHumanRemovesLifelink() {
        Permanent cleaver = harness.addToBattlefieldAndReturn(player1, new ButchersCleaver());
        Permanent human = addCreatureReady(player1, new HamletCaptain());
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        cleaver.setAttachedTo(human.getId());

        assertThat(gqs.hasKeyword(gd, human, Keyword.LIFELINK)).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(cleaver.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, human, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Moving Cleaver from non-Human to Human grants lifelink")
    void movingFromNonHumanToHumanGrantsLifelink() {
        Permanent cleaver = harness.addToBattlefieldAndReturn(player1, new ButchersCleaver());
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent human = addCreatureReady(player1, new HamletCaptain());
        cleaver.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, human.getId());
        harness.passBothPriorities();

        assertThat(cleaver.getAttachedTo()).isEqualTo(human.getId());
        assertThat(gqs.hasKeyword(gd, human, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(5);
    }

    @Test
    @DisplayName("Transforming an equipped Human into a non-Human removes lifelink but retains the boost")
    void transformingHumanRemovesLifelink() {
        Permanent ranger = addCreatureReady(player1, new DaybreakRanger());
        Permanent cleaver = harness.addToBattlefieldAndReturn(player1, new ButchersCleaver());
        cleaver.setAttachedTo(ranger.getId());

        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, ranger, Keyword.LIFELINK)).isTrue();

        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(ranger.isTransformed()).isTrue();
        assertThat(cleaver.getAttachedTo()).isEqualTo(ranger.getId());
        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ranger, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Unattached Cleaver grants neither a boost nor lifelink")
    void unattachedCleaverDoesNotAffectCreatures() {
        Permanent human = addCreatureReady(player1, new HamletCaptain());
        Permanent zombie = addCreatureReady(player1, new WalkingCorpse());
        harness.addToBattlefield(player1, new ButchersCleaver());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, human, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Unattaching Cleaver immediately removes both benefits")
    void unattachingCleaverRemovesBenefits() {
        Permanent human = addCreatureReady(player1, new HamletCaptain());
        Permanent cleaver = harness.addToBattlefieldAndReturn(player1, new ButchersCleaver());
        cleaver.setAttachedTo(human.getId());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, human, Keyword.LIFELINK)).isTrue();

        cleaver.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, human, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Two Cleavers stack their boosts but do not multiply lifelink life gain")
    void multipleCleaversDoNotMultiplyLifelink() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent human = addCreatureReady(player1, new HamletCaptain());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ButchersCleaver());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ButchersCleaver());
        first.setAttachedTo(human.getId());
        second.setAttachedTo(human.getId());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(2);
        human.setAttacking(true);
        resolveCombat();

        harness.assertLife(player1, 28);
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Lifelink benefits the equipped creature's controller, even across battlefields")
    void opponentControlledHumanGetsBenefits() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent human = addCreatureReady(player2, new HamletCaptain());
        Permanent cleaver = harness.addToBattlefieldAndReturn(player1, new ButchersCleaver());
        cleaver.setAttachedTo(human.getId());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, human, Keyword.LIFELINK)).isTrue();
        human.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 25);
    }
}
