package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FemerefArchers;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
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

@CardUsed({AirElemental.class, CentaurArcher.class, CircleOfProtectionGreen.class, FemerefArchers.class, GrizzlyBears.class, Hurricane.class, MerfolkOfThePearlTrident.class})
class CircleOfProtectionGreenTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability prompts for a green source choice")
    void resolvingAbilityPromptsForGreenSource() {
        addReadyCircle(player1);
        addReadyGreenCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Only green sources are offered when other colors are present")
    void onlyGreenSourcesAreValidChoices() {
        addReadyCircle(player1);
        Permanent greenSource = addReadyGreenCreature(player2);
        Permanent nonGreenSource = addReadyNonGreenCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(greenSource.getId()).doesNotContain(nonGreenSource.getId());
    }

    @Test
    @DisplayName("Multicolored sources with green in their colors are valid choices")
    void multicoloredGreenSourceIsValidChoice() {
        addReadyCircle(player1);
        Permanent redGreenSource = addCreatureReady(player2, new CentaurArcher());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(redGreenSource.getId());
    }

    @Test
    @DisplayName("Choosing a green source records a one-shot prevention shield")
    void choosingGreenSourceRecordsShield() {
        addReadyCircle(player1);
        Permanent bears = addReadyGreenCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId()) && s.sourceId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Prevents the next combat damage from the chosen source and consumes the shield")
    void preventsNextCombatDamageAndConsumesShield() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent bears = addReadyGreenCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        bears.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Only the chosen source is prevented; a different green source still deals damage")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent chosen = addReadyGreenCreature(player2);
        Permanent other = addReadyGreenCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        // The unchosen 2/2 deals its damage; the shield is untouched
        harness.assertLife(player1, 18);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.sourceId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("Non-green permanents are not valid source choices")
    void nonGreenSourceNotValid() {
        addReadyCircle(player1);
        addReadyNonGreenCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
        assertThat(gameLogContains("No permanents on the battlefield")).isTrue();
    }

    @Test
    @DisplayName("Prevents the next noncombat damage from a green spell on the stack")
    void preventsNextNoncombatDamageFromGreenSpell() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Hurricane hurricane = new Hurricane();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(hurricane));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player2, 0, 1);
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(hurricane.getId());

        harness.handlePermanentChosen(player1, hurricane.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        addReadyCircle(player1);
        Permanent bears = addReadyGreenCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    private Permanent addReadyCircle(Player player) {
        return addCreatureReady(player, new CircleOfProtectionGreen());
    }

    @Test
    void chosenSourceDamageToControlledCreatureIsNotPrevented() {
        addReadyCircle(player1);
        Permanent target = addCreatureReady(player1, new AirElemental());
        target.setAttacking(true);
        Permanent archers = addCreatureReady(player2, new FemerefArchers());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, archers.getId());

        harness.activateAbility(player2, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerSourceNextDamageShields)
                .filteredOn(s -> s.playerId().equals(player1.getId()))
                .anyMatch(s -> s.sourceId().equals(archers.getId()));
    }

    private Permanent addReadyGreenCreature(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private Permanent addReadyNonGreenCreature(Player player) {
        return addCreatureReady(player, new MerfolkOfThePearlTrident());
    }

    @Test
    @DisplayName("Prevents damage from a green spell chosen while it is on the stack")
    void preventsDamageFromGreenSpellOnStack() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addReadyCircle(player1);
        Hurricane hurricane = new Hurricane();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(hurricane));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player2, 0, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(hurricane.getId());
        harness.handlePermanentChosen(player1, hurricane.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }
}
