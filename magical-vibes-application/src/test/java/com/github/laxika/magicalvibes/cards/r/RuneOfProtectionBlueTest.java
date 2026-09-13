package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.e.ElvishLyrist;
import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
import com.github.laxika.magicalvibes.cards.h.HorseshoeCrab;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuneOfProtectionBlue.class, HorseshoeCrab.class, ElvishLyrist.class,
        CoralMerfolk.class, HermeticStudy.class})
class RuneOfProtectionBlueTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability prompts for a blue source choice")
    void resolvingAbilityPromptsForBlueSource() {
        addReadyRune(player1);
        Permanent crab = addReadyBlueCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(crab.getId());
    }

    @Test
    @DisplayName("Choosing a blue source records a one-shot prevention shield")
    void choosingBlueSourceRecordsShield() {
        addReadyRune(player1);
        Permanent wizard = addReadyBlueCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, wizard.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId()) && s.sourceId().equals(wizard.getId()));
    }

    @Test
    @DisplayName("Prevents the next combat damage from the chosen source and consumes the shield")
    void preventsNextCombatDamageAndConsumesShield() {
        harness.setLife(player1, 20);
        addReadyRune(player1);
        Permanent wizard = addReadyBlueCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, wizard.getId());

        wizard.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Only the chosen source is prevented; a different blue source still deals damage")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        addReadyRune(player1);
        Permanent chosen = addReadyBlueCreature(player2);
        Permanent other = addReadyBlueCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 19);
        assertThat(gd.playerSourceNextDamageShields).anyMatch(s -> s.sourceId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("Prevents the next noncombat damage from the chosen source")
    void preventsNextNoncombatDamage() {
        harness.setLife(player1, 20);
        addReadyRune(player1);
        Permanent merfolk = addCreatureReady(player2, new CoralMerfolk());
        Permanent study = harness.addToBattlefieldAndReturn(player2, new HermeticStudy());
        study.setAttachedTo(merfolk.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, merfolk.getId());

        int merfolkIndex = gd.playerBattlefields.get(player2.getId()).indexOf(merfolk);
        harness.activateAbility(player2, merfolkIndex, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("A prevention shield expires at the end of the turn")
    void shieldClearedAtEndOfTurn() {
        addReadyRune(player1);
        Permanent crab = addReadyBlueCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, crab.getId());

        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Can choose a blue spell on the stack as the source")
    void canChooseBlueSpellOnStack() {
        addReadyRune(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        HorseshoeCrab blueSpell = new HorseshoeCrab();
        harness.castFromHand(player2, blueSpell, "{2}{U}");
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(blueSpell.getId());

        harness.handlePermanentChosen(player1, blueSpell.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId()) && s.sourceId().equals(blueSpell.getId()));
    }

    @Test
    @DisplayName("Non-blue permanents are not valid source choices")
    void nonBlueSourceNotValid() {
        addReadyRune(player1);
        addReadyNonBlueCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
        assertThat(gameLogContains("No permanents on the battlefield")).isTrue();
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new RuneOfProtectionBlue()));
        harness.setLibrary(player1, List.of(new ElvishLyrist()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rune of Protection: Blue");
        harness.assertInHand(player1, "Elvish Lyrist");
    }

    private Permanent addReadyRune(Player player) {
        return addCreatureReady(player, new RuneOfProtectionBlue());
    }

    private Permanent addReadyBlueCreature(Player player) {
        return addCreatureReady(player, new HorseshoeCrab());
    }

    private Permanent addReadyNonBlueCreature(Player player) {
        return addCreatureReady(player, new ElvishLyrist());
    }
}
