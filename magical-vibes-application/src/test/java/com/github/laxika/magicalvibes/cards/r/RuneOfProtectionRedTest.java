package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.ArcLightning;
import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GoblinRaider;
import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuneOfProtectionRed.class, GoblinRaider.class, CoralMerfolk.class,
        ArcLightning.class, HermeticStudy.class})
class RuneOfProtectionRedTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability prompts for a red source choice")
    void resolvingAbilityPromptsForRedSource() {
        addReadyRune(player1);
        Permanent redSource = addReadyRedCreature(player2);
        Permanent nonRedSource = addReadyNonRedCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(redSource.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(nonRedSource.getId());
    }

    @Test
    @DisplayName("Prevents the next combat damage from the chosen red source")
    void preventsNextCombatDamageFromChosenSource() {
        harness.setLife(player1, 20);
        addReadyRune(player1);
        Permanent goblin = addReadyRedCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        goblin.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Only the chosen red source is prevented")
    void differentRedSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        addReadyRune(player1);
        Permanent chosen = addReadyRedCreature(player2);
        Permanent other = addReadyRedCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Non-red permanents are not valid source choices")
    void nonRedSourceNotValid() {
        addReadyRune(player1);
        addReadyNonRedCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents the next noncombat damage from the chosen red source")
    void preventsNextNoncombatDamageFromChosenSource() {
        harness.setLife(player1, 20);
        addReadyRune(player1);
        Permanent goblin = addReadyRedCreature(player2);
        Permanent study = harness.addToBattlefieldAndReturn(player2, new HermeticStudy());
        study.setAttachedTo(goblin.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        int goblinIndex = gd.playerBattlefields.get(player2.getId()).indexOf(goblin);
        harness.activateAbility(player2, goblinIndex, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Can choose a red spell on the stack as the source")
    void canChooseRedSpellOnStack() {
        harness.setLife(player1, 20);
        addReadyRune(player1);
        ArcLightning redSpell = new ArcLightning();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(redSpell));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castSorcery(player2, 0, Map.of(player1.getId(), 3));
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(redSpell.getId());

        harness.handlePermanentChosen(player1, redSpell.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("The prevention shield expires at the end of the turn")
    void shieldClearedAtEndOfTurn() {
        addReadyRune(player1);
        Permanent goblin = addReadyRedCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

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
        RuneOfProtectionRed rune = new RuneOfProtectionRed();
        CoralMerfolk merfolk = new CoralMerfolk();
        harness.setHand(player1, List.of(rune));
        harness.setLibrary(player1, List.of(merfolk));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rune);
        assertThat(gd.playerHands.get(player1.getId())).contains(merfolk);
    }

    private Permanent addReadyRune(Player player) {
        return addCreatureReady(player, new RuneOfProtectionRed());
    }

    private Permanent addReadyRedCreature(Player player) {
        return addCreatureReady(player, new GoblinRaider());
    }

    private Permanent addReadyNonRedCreature(Player player) {
        return addCreatureReady(player, new CoralMerfolk());
    }
}
