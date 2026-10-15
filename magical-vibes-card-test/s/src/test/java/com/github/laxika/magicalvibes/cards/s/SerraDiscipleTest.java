package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BlackbladeReforged;
import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerraDisciple.class, Spellbook.class, GrizzlyBears.class,
        AdelizTheCinderWind.class, HistoryOfBenalia.class, BlackbladeReforged.class})
class SerraDiscipleTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an artifact triggers +1/+1 boost")
    void artifactSpellTriggersBoost() {
        harness.addToBattlefield(player1, new SerraDisciple());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Serra Disciple"));
    }

    @Test
    @DisplayName("Resolving artifact-triggered ability gives +1/+1 until end of turn")
    void artifactTriggerBoosts() {
        harness.addToBattlefield(player1, new SerraDisciple());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();

        Permanent disciple = findPermanent(player1, "Serra Disciple");
        assertThat(disciple.getPowerModifier()).isEqualTo(1);
        assertThat(disciple.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a legendary creature triggers +1/+1 boost")
    void legendarySpellTriggersBoost() {
        harness.addToBattlefield(player1, new SerraDisciple());
        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Serra Disciple"));
    }

    @Test
    @DisplayName("Casting a non-historic creature does not trigger boost")
    void nonHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new SerraDisciple());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Opponent casting an artifact does not trigger controller's Serra Disciple")
    void opponentHistoricDoesNotTrigger() {
        harness.addToBattlefield(player1, new SerraDisciple());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Spellbook()));

        harness.castArtifact(player2, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    @DisplayName("Casting two historic spells gives +2/+2 total")
    void multipleHistoricSpellsStack() {
        harness.addToBattlefield(player1, new SerraDisciple());
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));

        // Cast first artifact
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        // Cast second artifact
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent disciple = findPermanent(player1, "Serra Disciple");
        assertThat(disciple.getPowerModifier()).isEqualTo(2);
        assertThat(disciple.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("A Saga cast boosts Serra Disciple before the Saga resolves")
    void sagaCastBoostsBeforeResolution() {
        harness.addToBattlefield(player1, new SerraDisciple());
        harness.setHand(player1, List.of(new HistoryOfBenalia()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        Permanent disciple = findPermanent(player1, "Serra Disciple");
        assertThat(disciple.getPowerModifier()).isEqualTo(1);
        assertThat(disciple.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell that is both legendary and an artifact triggers only once")
    void legendaryArtifactTriggersOnce() {
        harness.addToBattlefield(player1, new SerraDisciple());
        harness.setHand(player1, List.of(new BlackbladeReforged()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        Permanent disciple = findPermanent(player1, "Serra Disciple");
        assertThat(disciple.getPowerModifier()).isEqualTo(1);
        assertThat(disciple.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Serra Disciple boosts itself independently")
    void eachDiscipleBoostsItself() {
        harness.addToBattlefield(player1, new SerraDisciple());
        harness.addToBattlefield(player1, new SerraDisciple());
        harness.setHand(player1, List.of(new BlackbladeReforged()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(3);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Serra Disciple")).allSatisfy(disciple -> {
            assertThat(disciple.getPowerModifier()).isEqualTo(1);
            assertThat(disciple.getToughnessModifier()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Putting a historic permanent onto the battlefield does not trigger")
    void enteringWithoutCastingDoesNotTrigger() {
        harness.addToBattlefield(player1, new SerraDisciple());
        harness.addToBattlefield(player1, new BlackbladeReforged());

        assertThat(gd.stack).isEmpty();
        Permanent disciple = findPermanent(player1, "Serra Disciple");
        assertThat(disciple.getPowerModifier()).isZero();
        assertThat(disciple.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost persists through the end step and expires during cleanup")
    void boostExpiresDuringCleanup() {
        harness.addToBattlefield(player1, new SerraDisciple());
        harness.setHand(player1, List.of(new BlackbladeReforged()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        Permanent disciple = findPermanent(player1, "Serra Disciple");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(disciple.getPowerModifier()).isEqualTo(1);
        assertThat(disciple.getToughnessModifier()).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(disciple.getPowerModifier()).isZero();
        assertThat(disciple.getToughnessModifier()).isZero();
    }
}
