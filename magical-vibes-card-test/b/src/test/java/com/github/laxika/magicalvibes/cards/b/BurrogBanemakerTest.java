package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BurrogBanemaker.class})
class BurrogBanemakerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Burrog Banemaker puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new BurrogBanemaker()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Burrog Banemaker");
    }

    @Test
    @DisplayName("Resolving Burrog Banemaker puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new BurrogBanemaker()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Burrog Banemaker"));
    }

    @Test
    @DisplayName("Activating ability puts BoostSelf on the stack with self as target")
    void activatingAbilityPutsOnStack() {
        Permanent banemakerPerm = addCreatureReady(player1, new BurrogBanemaker());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Burrog Banemaker");
        assertThat(entry.getTargetId()).isEqualTo(banemakerPerm.getId());
    }

    @Test
    @DisplayName("Resolving ability gives +1/+1 to Burrog Banemaker")
    void resolvingAbilityBoostsPowerAndToughness() {
        addCreatureReady(player1, new BurrogBanemaker());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent banemaker = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, banemaker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, banemaker)).isEqualTo(2);
        assertThat(PermanentGrants.powerBoostOf(gd, banemaker)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, banemaker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate ability multiple times if mana allows")
    void canActivateMultipleTimes() {
        addCreatureReady(player1, new BurrogBanemaker());
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent banemaker = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, banemaker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, banemaker)).isEqualTo(4);
        assertThat(PermanentGrants.powerBoostOf(gd, banemaker)).isEqualTo(3);
        assertThat(PermanentGrants.toughnessBoostOf(gd, banemaker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addCreatureReady(player1, new BurrogBanemaker());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent banemaker = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, banemaker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, banemaker)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, banemaker)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, banemaker)).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, banemaker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, banemaker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new BurrogBanemaker());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent banemaker = harness.addToBattlefieldAndReturn(player1, new BurrogBanemaker());
        banemaker.setSummoningSick(true);
        banemaker.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, banemaker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, banemaker)).isEqualTo(2);
        assertThat(banemaker.isTapped()).isTrue();
    }

    @Test
    void cannotPayBlackRequirementWithOnlyColorlessMana() {
        addCreatureReady(player1, new BurrogBanemaker());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void stackedActivationsBoostOnlyTheirSource() {
        Permanent source = addCreatureReady(player1, new BurrogBanemaker());
        Permanent other = addCreatureReady(player1, new BurrogBanemaker());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(3);
        assertThat(PermanentGrants.powerBoostOf(gd, other)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, other)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void deathtouchKillsBlockerWithMoreToughnessThanDamageDealt() {
        addCreatureReady(player1, new BurrogBanemaker());
        Permanent blocker = addCreatureReady(player2, new BurrogBanemaker());
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(3);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Burrog Banemaker");
        harness.assertNotOnBattlefield(player1, "Burrog Banemaker");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
