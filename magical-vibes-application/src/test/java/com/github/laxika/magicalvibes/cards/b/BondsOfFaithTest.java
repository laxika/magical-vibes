package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.t.TravelersAmulet;
import com.github.laxika.magicalvibes.cards.s.SilverchaseFox;
import com.github.laxika.magicalvibes.cards.d.DoomedTraveler;
import com.github.laxika.magicalvibes.cards.m.MayorOfAvabruck;
import com.github.laxika.magicalvibes.cards.m.Moonmist;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BondsOfFaith.class, SilverchaseFox.class, DoomedTraveler.class, TravelersAmulet.class, MayorOfAvabruck.class, Moonmist.class})
class BondsOfFaithTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Bonds of Faith attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new SilverchaseFox());
        bearsPerm.setSummoningSick(false);

        harness.setHand(player1, List.of(new BondsOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Bonds of Faith")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Human creature enchanted with Bonds of Faith gets +2/+2")
    void humanCreatureGetsBoost() {
        Permanent humanPerm = harness.addToBattlefieldAndReturn(player1, new DoomedTraveler());
        humanPerm.setSummoningSick(false);

        // Attach Bonds of Faith directly
        Permanent bondsPerm = harness.addToBattlefieldAndReturn(player1, new BondsOfFaith());
        bondsPerm.setAttachedTo(humanPerm.getId());

        // Doomed Traveler is 1/1; with +2/+2 should be 3/3
        assertThat(gqs.getEffectivePower(gd, humanPerm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, humanPerm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Human creature enchanted with Bonds of Faith can still attack")
    void humanCreatureCanAttack() {
        Permanent humanPerm = harness.addToBattlefieldAndReturn(player1, new DoomedTraveler());
        humanPerm.setSummoningSick(false);

        Permanent bondsPerm = harness.addToBattlefieldAndReturn(player1, new BondsOfFaith());
        bondsPerm.setAttachedTo(humanPerm.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Human creature should be able to attack — call succeeding proves it
        gs.declareAttackers(gd, player1, List.of(0));
    }

    @Test
    @DisplayName("Human creature enchanted with Bonds of Faith can still block")
    void humanCreatureCanBlock() {
        Permanent humanPerm = harness.addToBattlefieldAndReturn(player2, new DoomedTraveler());
        humanPerm.setSummoningSick(false);

        Permanent bondsPerm = harness.addToBattlefieldAndReturn(player2, new BondsOfFaith());
        bondsPerm.setAttachedTo(humanPerm.getId());

        // Player1 has an attacker
        Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new SilverchaseFox());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        // Human creature should be able to block
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(humanPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Non-Human creature enchanted with Bonds of Faith does not get +2/+2")
    void nonHumanCreatureDoesNotGetBoost() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new SilverchaseFox());
        bearsPerm.setSummoningSick(false);

        Permanent bondsPerm = harness.addToBattlefieldAndReturn(player1, new BondsOfFaith());
        bondsPerm.setAttachedTo(bearsPerm.getId());

        // Silverchase Fox is 2/2, should remain 2/2 (no boost for non-Human)
        assertThat(gqs.getEffectivePower(gd, bearsPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bearsPerm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-Human creature enchanted with Bonds of Faith cannot attack")
    void nonHumanCreatureCannotAttack() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new SilverchaseFox());
        bearsPerm.setSummoningSick(false);

        Permanent bondsPerm = harness.addToBattlefieldAndReturn(player2, new BondsOfFaith());
        bondsPerm.setAttachedTo(bearsPerm.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Non-Human creature enchanted with Bonds of Faith cannot block")
    void nonHumanCreatureCannotBlock() {
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new SilverchaseFox());
        blockerPerm.setSummoningSick(false);

        Permanent bondsPerm = harness.addToBattlefieldAndReturn(player1, new BondsOfFaith());
        bondsPerm.setAttachedTo(blockerPerm.getId());

        // Player1 has an attacker (index 1, after Bonds at index 0)
        Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new SilverchaseFox());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Non-Human creature can attack again after Bonds of Faith is removed")
    void nonHumanCanAttackAfterBondsRemoved() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new SilverchaseFox());
        bearsPerm.setSummoningSick(false);

        Permanent bondsPerm = harness.addToBattlefieldAndReturn(player2, new BondsOfFaith());
        bondsPerm.setAttachedTo(bearsPerm.getId());

        // Verify creature can't attack
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        // Remove Bonds of Faith
        gd.playerBattlefields.get(player2.getId()).remove(bondsPerm);

        // Now creature can attack
        harness.beginAttackerDeclarationInput();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gs.declareAttackers(gd, player1, List.of(0));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Bonds of Faith")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new TravelersAmulet());
        harness.setHand(player1, List.of(new BondsOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        Permanent artifact = findPermanent(player1, "Traveler's Amulet");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Bonds of Faith fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new SilverchaseFox());
        bearsPerm.setSummoningSick(false);

        harness.setHand(player1, List.of(new BondsOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        // Remove the target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bonds of Faith");
        harness.assertNotOnBattlefield(player1, "Bonds of Faith");
    }

    @Test
    @DisplayName("An opponent's Human gets the boost and loses it when the Aura leaves")
    void opposingHumanLosesBoostWhenAuraLeaves() {
        Permanent human = harness.addToBattlefieldAndReturn(player2, new DoomedTraveler());
        harness.setHand(player1, List.of(new BondsOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, human.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Bonds of Faith"));

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
    }

    @Test
    @DisplayName("Transforming a Human into a non-Human switches the boost to combat restrictions")
    void transformingHumanSwitchesToRestriction() {
        Permanent mayor = harness.addToBattlefieldAndReturn(player1, new MayorOfAvabruck());
        mayor.setSummoningSick(false);
        harness.setHand(player1, List.of(new BondsOfFaith(), new Moonmist()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, mayor.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mayor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mayor)).isEqualTo(3);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0);

        assertThat(mayor.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, mayor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mayor)).isEqualTo(3);
        assertThat(findPermanent(player1, "Bonds of Faith").getAttachedTo()).isEqualTo(mayor.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }
}
