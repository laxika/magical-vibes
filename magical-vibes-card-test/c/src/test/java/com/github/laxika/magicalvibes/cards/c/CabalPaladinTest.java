package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.cards.b.BlackbladeReforged;
import com.github.laxika.magicalvibes.cards.t.TheFlameOfKeld;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CabalPaladin.class, AdelizTheCinderWind.class, GrizzlyBears.class, Spellbook.class,
        BlackbladeReforged.class, TheFlameOfKeld.class})
class CabalPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an artifact triggers 2 damage to each opponent")
    void artifactSpellTriggersDamage() {
        harness.addToBattlefield(player1, new CabalPaladin());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        // Spellbook on stack + triggered ability
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Cabal Paladin"));
    }

    @Test
    @DisplayName("Resolving artifact-triggered ability deals 2 damage to opponent")
    void artifactTriggerDealsDamage() {
        harness.addToBattlefield(player1, new CabalPaladin());
        harness.setHand(player1, List.of(new Spellbook()));

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());

        harness.castArtifact(player1, 0);
        // Resolve the triggered ability (LIFO — trigger on top, Spellbook below)
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        // Controller's life should be unchanged
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Casting a legendary creature triggers 2 damage to each opponent")
    void legendarySpellTriggersDamage() {
        harness.addToBattlefield(player1, new CabalPaladin());
        harness.castFromHand(player1, new AdelizTheCinderWind(), "{1}{U}{R}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Cabal Paladin"));
    }

    @Test
    @DisplayName("Casting a non-historic creature does not trigger damage")
    void nonHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new CabalPaladin());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        GameData gd = harness.getGameData();
        // Only the creature spell should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Opponent casting an artifact does not trigger controller's Cabal Paladin")
    void opponentHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new CabalPaladin());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Spellbook()));

        harness.castArtifact(player2, 0);

        GameData gd = harness.getGameData();
        // Only the artifact spell on stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    @DisplayName("Casting two artifact spells triggers damage each time")
    void multipleHistoricSpellsTriggerMultipleTimes() {
        harness.addToBattlefield(player1, new CabalPaladin());
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());

        // Cast first artifact
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve triggered ability (damage)
        harness.passBothPriorities(); // resolve Spellbook

        // Cast second artifact
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve triggered ability (damage)

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Casting a nonlegendary Saga deals damage before the Saga resolves")
    void sagaSpellTriggersDamage() {
        harness.addToBattlefield(player1, new CabalPaladin());

        harness.castFromHand(player1, new TheFlameOfKeld(), "{1}{R}");

        assertThat(harness.getGameData().stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        assertThat(harness.getGameData().stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "The Flame of Keld");
    }

    @Test
    @DisplayName("A legendary artifact triggers only once despite two historic categories")
    void legendaryArtifactTriggersOnlyOnce() {
        harness.addToBattlefield(player1, new CabalPaladin());

        harness.castFromHand(player1, new BlackbladeReforged(), "{2}");

        assertThat(harness.getGameData().stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Each Cabal Paladin triggers separately for a historic spell")
    void multiplePaladinsEachDealDamage() {
        harness.addToBattlefield(player1, new CabalPaladin());
        harness.addToBattlefield(player1, new CabalPaladin());

        harness.castFromHand(player1, new BlackbladeReforged(), "{2}");

        assertThat(harness.getGameData().stack).hasSize(3);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
        assertThat(harness.getGameData().stack).hasSize(1);
    }
}
