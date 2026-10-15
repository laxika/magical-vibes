package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GemOfBecoming;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CripplingBlight.class, WalkingCorpse.class, GemOfBecoming.class})
class CripplingBlightTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets -1/-1")
    void enchantedCreatureGetsMinusOneMinusOne() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        harness.setHand(player1, List.of(new CripplingBlight()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off when the Aura leaves the battlefield")
    void boostGoesAwayWhenAuraRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        harness.setHand(player1, List.of(new CripplingBlight()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Crippling Blight");
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enchanted creature can't be declared as a blocker")
    void enchantedCreatureCannotBlock() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        blocker.setSummoningSick(false);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CripplingBlight());
        aura.setAttachedTo(blocker.getId());

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Enchanted creature can still attack")
    void enchantedCreatureCanStillAttack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        creature.setSummoningSick(false);

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new CripplingBlight());
        aura.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
    }

    @Test
    @DisplayName("Creature can block again after the Aura is removed")
    void creatureCanBlockAfterAuraRemoved() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        blocker.setSummoningSick(false);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CripplingBlight());
        aura.setAttachedTo(blocker.getId());

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GemOfBecoming());
        harness.setHand(player1, List.of(new CripplingBlight()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can enchant your own creature without shrinking other creatures")
    void onlyEnchantedCreatureIsShrunk() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new CripplingBlight()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Blights stack and zero toughness puts the creature and Auras into graveyards")
    void multipleBlightsKillCreatureWithZeroToughness() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new CripplingBlight(), new CripplingBlight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Walking Corpse");
        harness.assertInGraveyard(player2, "Walking Corpse");
        harness.assertNotOnBattlefield(player1, "Crippling Blight");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof CripplingBlight)
                .hasSize(2);
    }
}
