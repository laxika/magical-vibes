package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Brushland;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NaturalAffinity;
import com.github.laxika.magicalvibes.cards.o.OneWithTheStars;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuneOfProtectionLands.class, Brushland.class, GrizzlyBears.class, NaturalAffinity.class, OneWithTheStars.class})
class RuneOfProtectionLandsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability prompts for a land source choice")
    void resolvingAbilityPromptsForLandSource() {
        addCreatureReady(player1, new RuneOfProtectionLands());
        addCreatureReady(player2, new Brushland());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Choosing a land source records a one-shot prevention shield")
    void choosingLandSourceRecordsShield() {
        addCreatureReady(player1, new RuneOfProtectionLands());
        Permanent land = addCreatureReady(player2, new Brushland());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId()) && s.sourceId().equals(land.getId()));
    }

    @Test
    @DisplayName("Prevents the next damage from the chosen land source and consumes the shield")
    void preventsNextDamageFromLandSource() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new RuneOfProtectionLands());
        Permanent brushland = addCreatureReady(player1, new Brushland());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, brushland.getId());

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Non-land permanents are not valid source choices")
    void nonLandSourceNotValid() {
        addCreatureReady(player1, new RuneOfProtectionLands());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("No permanents on the battlefield"));
    }

    @Test
    @DisplayName("Only land permanents are offered when nonlands are also present")
    void onlyLandPermanentsAreValidSourceChoices() {
        addCreatureReady(player1, new RuneOfProtectionLands());
        Permanent land = addCreatureReady(player2, new Brushland());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(land.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(creature.getId());
    }

    @Test
    @DisplayName("The shield applies only to the chosen land's next damage")
    void shieldIsSpecificToChosenLandAndNextDamage() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new RuneOfProtectionLands());
        Permanent chosenLand = addCreatureReady(player1, new Brushland());
        addCreatureReady(player1, new Brushland());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosenLand.getId());

        harness.activateAbility(player1, 2, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.playerSourceNextDamageShields).hasSize(1);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("A selected source must still be a land when it deals damage")
    void sourceMustStillMatchLandFilterWhenDamageIsDealt() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new RuneOfProtectionLands());
        Permanent land = addCreatureReady(player1, new Brushland());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());

        harness.setHand(player1, List.of(new NaturalAffinity()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.setHand(player1, List.of(new OneWithTheStars()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, land)).isFalse();

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.playerSourceNextDamageShields).hasSize(1);
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        addCreatureReady(player1, new RuneOfProtectionLands());
        Permanent land = addCreatureReady(player2, new Brushland());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());

        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new RuneOfProtectionLands()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rune of Protection: Lands");
        harness.assertInHand(player1, "Grizzly Bears");
    }

}
