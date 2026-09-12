package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FleshReaver;
import com.github.laxika.magicalvibes.cards.s.SanguineGuard;
import com.github.laxika.magicalvibes.cards.s.SerraZealot;
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

@CardUsed({RuneOfProtectionBlack.class, SanguineGuard.class, SerraZealot.class, FleshReaver.class})
class RuneOfProtectionBlackTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability prompts for a black source choice")
    void resolvingAbilityPromptsForBlackSource() {
        addReadyRune(player1);
        Permanent blackSource = addReadyBlackCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(blackSource.getId());
    }

    @Test
    @DisplayName("Choosing a black source records a one-shot prevention shield")
    void choosingBlackSourceRecordsShield() {
        addReadyRune(player1);
        Permanent zombie = addReadyBlackCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, zombie.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId()) && s.sourceId().equals(zombie.getId()));
    }

    @Test
    @DisplayName("Prevents the next combat damage from the chosen source and consumes the shield")
    void preventsNextCombatDamageAndConsumesShield() {
        harness.setLife(player1, 20);
        addReadyRune(player1);
        Permanent zombie = addReadyBlackCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, zombie.getId());

        zombie.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Only the chosen source is prevented; a different black source still deals damage")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        addReadyRune(player1);
        Permanent chosen = addReadyBlackCreature(player2);
        Permanent other = addReadyBlackCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        assertThat(gd.playerSourceNextDamageShields).anyMatch(s -> s.sourceId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("Prevents the next noncombat damage from the chosen black source")
    void preventsNextNoncombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addReadyRune(player1);
        Permanent fleshReaver = addCreatureReady(player1, new FleshReaver());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, fleshReaver.getId());

        fleshReaver.setAttacking(true);
        resolveCombat(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Can choose a black spell on the stack as the source")
    void canChooseBlackSpellOnStack() {
        addReadyRune(player1);
        SanguineGuard blackSpell = new SanguineGuard();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, blackSpell, "{1}{B}{B}");
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(blackSpell.getId());

        harness.handlePermanentChosen(player1, blackSpell.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId()) && s.sourceId().equals(blackSpell.getId()));
    }

    @Test
    @DisplayName("Non-black permanents are not valid source choices")
    void nonBlackSourceNotValid() {
        addReadyRune(player1);
        addReadyNonBlackCreature(player2);
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
        harness.setHand(player1, List.of(new RuneOfProtectionBlack()));
        harness.setLibrary(player1, List.of(new SerraZealot()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rune of Protection: Black");
        harness.assertInHand(player1, "Serra Zealot");
    }

    private Permanent addReadyRune(Player player) {
        return addCreatureReady(player, new RuneOfProtectionBlack());
    }

    private Permanent addReadyBlackCreature(Player player) {
        return addCreatureReady(player, new SanguineGuard());
    }

    private Permanent addReadyNonBlackCreature(Player player) {
        return addCreatureReady(player, new SerraZealot());
    }
}
