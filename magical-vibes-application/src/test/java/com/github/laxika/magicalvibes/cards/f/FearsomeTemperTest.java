package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.o.OreskosSunGuide;
import com.github.laxika.magicalvibes.cards.s.SirenSongLyre;
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

@CardUsed({FearsomeTemper.class, OreskosSunGuide.class, SirenSongLyre.class})
class FearsomeTemperTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent creature = addEnchantedCreature();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enchanted creature can make a target creature unable to block it")
    void grantedAbilityPreventsTargetFromBlockingEnchantedCreature() {
        Permanent creature = addEnchantedCreature();
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new OreskosSunGuide());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getCantBlockIds()).contains(creature.getId());
    }

    @Test
    @DisplayName("Fearsome Temper's ability does not stop the target creature from blocking other creatures")
    void grantedAbilityOnlyRestrictsEnchantedCreature() {
        addEnchantedCreature();
        Permanent otherAttacker = harness.addToBattlefieldAndReturn(player1, new OreskosSunGuide());
        otherAttacker.setSummoningSick(false);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new OreskosSunGuide());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        otherAttacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(otherAttacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }

    @Test
    @DisplayName("Enchanted creature loses Fearsome Temper's ability when the Aura leaves")
    void abilityLostWhenAuraLeaves() {
        addEnchantedCreature();
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof FearsomeTemper);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Fearsome Temper cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SirenSongLyre());
        harness.setHand(player1, List.of(new FearsomeTemper()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The targeted creature cannot be declared as a blocker of the enchanted creature")
    void restrictedBlockIsIllegal() {
        Permanent creature = addEnchantedCreature();
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new OreskosSunGuide());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        creature.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted ability cannot target a noncreature")
    void grantedAbilityRejectsNonCreature() {
        addEnchantedCreature();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SirenSongLyre());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted ability can be activated while tapped and summoning sick")
    void abilityDoesNotRequireTapOrHaste() {
        Permanent creature = addEnchantedCreature();
        creature.setSummoningSick(true);
        creature.setTapped(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new OreskosSunGuide());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getCantBlockIds()).contains(creature.getId());
    }

    @Test
    @DisplayName("Removing the Aura does not counter an already activated ability")
    void abilityResolvesAfterAuraLeaves() {
        Permanent creature = addEnchantedCreature();
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new OreskosSunGuide());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, blocker.getId());
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof FearsomeTemper);
        harness.passBothPriorities();

        assertThat(blocker.getCantBlockIds()).contains(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's enchanted creature gets the boost and its controller activates the ability")
    void opponentControlsGrantedAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new OreskosSunGuide());
        Permanent blocker = harness.addToBattlefieldAndReturn(player1, new OreskosSunGuide());
        harness.setHand(player1, List.of(new FearsomeTemper()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getCantBlockIds()).contains(creature.getId());
    }

    @Test
    @DisplayName("The blocking restriction expires at the end of the turn")
    void restrictionExpiresAtEndOfTurn() {
        Permanent creature = addEnchantedCreature();
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new OreskosSunGuide());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();
        assertThat(blocker.getCantBlockIds()).contains(creature.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(blocker.getCantBlockIds()).doesNotContain(creature.getId());
    }

    private Permanent addEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new OreskosSunGuide());
        creature.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FearsomeTemper());
        aura.setAttachedTo(creature.getId());
        return creature;
    }
}
