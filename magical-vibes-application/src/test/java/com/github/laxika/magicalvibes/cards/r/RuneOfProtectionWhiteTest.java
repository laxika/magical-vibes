package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.h.HealingSalve;
import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
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

@CardUsed({RuneOfProtectionWhite.class, SerraZealot.class, CoralMerfolk.class,
        HermeticStudy.class, HealingSalve.class})
class RuneOfProtectionWhiteTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability prompts for a white source choice")
    void resolvingAbilityPromptsForWhiteSource() {
        Permanent rune = addReadyRune(player1);
        Permanent whiteSource = addReadyWhiteCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(rune.getId(), whiteSource.getId());
    }

    @Test
    @DisplayName("Prevents the next combat damage from the chosen white source and consumes the shield")
    void preventsNextCombatDamageAndConsumesShield() {
        harness.setLife(player1, 20);
        addReadyRune(player1);
        Permanent whiteSource = addReadyWhiteCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, whiteSource.getId());

        whiteSource.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Only the chosen white source is prevented")
    void differentWhiteSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        addReadyRune(player1);
        Permanent chosen = addReadyWhiteCreature(player2);
        Permanent other = addReadyWhiteCreature(player2);
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
    @DisplayName("Non-white permanents are excluded from source choices")
    void nonWhiteSourceNotValid() {
        Permanent rune = addReadyRune(player1);
        Permanent nonWhiteSource = addReadyNonWhiteCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(rune.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(nonWhiteSource.getId());
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents the next noncombat damage from the chosen white source")
    void preventsNextNoncombatDamageFromChosenSource() {
        harness.setLife(player1, 20);
        addReadyRune(player1);
        Permanent whiteSource = addReadyWhiteCreature(player2);
        Permanent study = harness.addToBattlefieldAndReturn(player2, new HermeticStudy());
        study.setAttachedTo(whiteSource.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, whiteSource.getId());

        int whiteSourceIndex = gd.playerBattlefields.get(player2.getId()).indexOf(whiteSource);
        harness.activateAbility(player2, whiteSourceIndex, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Can choose a white spell on the stack as the source")
    void canChooseWhiteSpellOnStack() {
        addReadyRune(player1);
        HealingSalve whiteSpell = new HealingSalve();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(whiteSpell));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, 0, player2.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(whiteSpell.getId());

        harness.handlePermanentChosen(player1, whiteSpell.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("The prevention shield expires at the end of the turn")
    void shieldClearedAtEndOfTurn() {
        addReadyRune(player1);
        Permanent whiteSource = addReadyWhiteCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, whiteSource.getId());

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
        harness.setHand(player1, List.of(new RuneOfProtectionWhite()));
        harness.setLibrary(player1, List.of(new CoralMerfolk()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rune of Protection: White");
        harness.assertInHand(player1, "Coral Merfolk");
    }

    private Permanent addReadyRune(Player player) {
        return addCreatureReady(player, new RuneOfProtectionWhite());
    }

    private Permanent addReadyWhiteCreature(Player player) {
        return addCreatureReady(player, new SerraZealot());
    }

    private Permanent addReadyNonWhiteCreature(Player player) {
        return addCreatureReady(player, new CoralMerfolk());
    }
}
