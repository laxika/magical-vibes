package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwordOfTheParuns.class, GrizzlyBears.class})
class SwordOfTheParunsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped equipped creature gives tapped creatures +2/+0")
    void tappedEquippedCreatureBoostsTappedCreatures() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTheParuns());
        Permanent tappedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent untappedCreature = addCreatureReady(player1, new GrizzlyBears());
        sword.setAttachedTo(tappedCreature.getId());
        tappedCreature.tap();

        assertThat(gqs.getEffectivePower(gd, tappedCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tappedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, untappedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, untappedCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Untapped equipped creature gives untapped creatures +0/+2")
    void untappedEquippedCreatureBoostsUntappedCreatures() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTheParuns());
        Permanent untappedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent tappedCreature = addCreatureReady(player1, new GrizzlyBears());
        sword.setAttachedTo(untappedCreature.getId());
        tappedCreature.tap();

        assertThat(gqs.getEffectivePower(gd, untappedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, untappedCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, tappedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tappedCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapped equipped creature boosts all tapped creatures I control, but not an opponent's")
    void tappedEquippedCreatureBoostsAllTappedControlledCreaturesOnly() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTheParuns());
        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherTappedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentTappedCreature = addCreatureReady(player2, new GrizzlyBears());
        sword.setAttachedTo(equippedCreature.getId());
        equippedCreature.tap();
        otherTappedCreature.tap();
        opponentTappedCreature.tap();

        assertThat(gqs.getEffectivePower(gd, equippedCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, otherTappedCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, otherTappedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentTappedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentTappedCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Untapped equipped creature boosts all untapped creatures I control, but not an opponent's")
    void untappedEquippedCreatureBoostsAllUntappedControlledCreaturesOnly() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTheParuns());
        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherUntappedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownTappedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentUntappedCreature = addCreatureReady(player2, new GrizzlyBears());
        sword.setAttachedTo(equippedCreature.getId());
        ownTappedCreature.tap();

        assertThat(gqs.getEffectivePower(gd, equippedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, equippedCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, otherUntappedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherUntappedCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownTappedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownTappedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentUntappedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentUntappedCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("An unattached Sword gives no creature bonus")
    void unattachedSwordGivesNoCreatureBonus() {
        harness.addToBattlefieldAndReturn(player1, new SwordOfTheParuns());
        Permanent tappedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent untappedCreature = addCreatureReady(player1, new GrizzlyBears());
        tappedCreature.tap();

        assertThat(gqs.getEffectivePower(gd, tappedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tappedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, untappedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, untappedCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The activated ability lets you choose to tap the equipped creature")
    void activatedAbilityCanTapEquippedCreature() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTheParuns());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        sword.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Tap equipped creature");

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability lets you choose to untap the equipped creature")
    void activatedAbilityCanUntapEquippedCreature() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTheParuns());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        sword.setAttachedTo(creature.getId());
        creature.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Untap equipped creature");

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Equip {3} attaches Sword of the Paruns to a creature I control")
    void equipAbilityAttachesSwordToControlledCreature() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTheParuns());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The tap-or-untap ability does nothing when Sword is not equipped")
    void activatedAbilityDoesNothingWhenSwordIsUnattached() {
        harness.addToBattlefieldAndReturn(player1, new SwordOfTheParuns());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Tap equipped creature");

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the activated ability does not change the equipped creature's tap state")
    void decliningActivatedAbilityDoesNothing() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfTheParuns());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        sword.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(creature.isTapped()).isFalse();
    }
}
