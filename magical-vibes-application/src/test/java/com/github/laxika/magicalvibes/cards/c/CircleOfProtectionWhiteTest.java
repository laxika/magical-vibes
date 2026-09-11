package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DAvenantArcher;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HealingSalve;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.j.Justice;
import com.github.laxika.magicalvibes.cards.p.PearledUnicorn;
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

@CardUsed({CircleOfProtectionWhite.class, DAvenantArcher.class, GrizzlyBears.class,
        HealingSalve.class, Incinerate.class, Justice.class, PearledUnicorn.class})
class CircleOfProtectionWhiteTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability prompts for a white source choice")
    void resolvingAbilityPromptsForWhiteSource() {
        addCreatureReady(player1, new CircleOfProtectionWhite());
        addCreatureReady(player2, new PearledUnicorn());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Only white sources are offered as source choices")
    void onlyWhiteSourcesAreOfferedAsChoices() {
        Permanent circle = addCreatureReady(player1, new CircleOfProtectionWhite());
        Permanent whiteSource = addCreatureReady(player2, new PearledUnicorn());
        Permanent greenSource = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(circle.getId(), whiteSource.getId());
        assertThat(choice.validIds()).doesNotContain(greenSource.getId());
    }

    @Test
    @DisplayName("Choosing a white source records a one-shot prevention shield")
    void choosingWhiteSourceRecordsShield() {
        addCreatureReady(player1, new CircleOfProtectionWhite());
        Permanent soldier = addCreatureReady(player2, new PearledUnicorn());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, soldier.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId()) && s.sourceId().equals(soldier.getId()));
    }

    @Test
    @DisplayName("Prevents the next combat damage from the chosen source and consumes the shield")
    void preventsNextCombatDamageAndConsumesShield() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new CircleOfProtectionWhite());
        Permanent soldier = addCreatureReady(player2, new PearledUnicorn());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, soldier.getId());

        soldier.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents the next noncombat damage from the chosen white source")
    void preventsNextNoncombatDamageFromChosenWhiteSource() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new CircleOfProtectionWhite());
        Permanent justice = harness.addToBattlefieldAndReturn(player2, new Justice());
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, justice.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Damage from the chosen source to your creature is not prevented")
    void chosenSourceDamageToControlledCreatureIsNotPrevented() {
        addCreatureReady(player1, new CircleOfProtectionWhite());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.setAttacking(true);
        Permanent archer = addCreatureReady(player2, new DAvenantArcher());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, archer.getId());

        harness.activateAbility(player2, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId()) && s.sourceId().equals(archer.getId()));
    }

    @Test
    @DisplayName("Only the chosen source is prevented; a different white source still deals damage")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new CircleOfProtectionWhite());
        Permanent chosen = addCreatureReady(player2, new PearledUnicorn());
        Permanent other = addCreatureReady(player2, new PearledUnicorn());
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
    @DisplayName("A non-white source is not a valid choice; the chosen white source is unaffected")
    void nonWhiteSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new CircleOfProtectionWhite());
        Permanent white = addCreatureReady(player2, new PearledUnicorn());
        Permanent green = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, white.getId());

        // The green creature isn't the chosen (white) source, so its damage is not prevented.
        green.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.sourceId().equals(white.getId()));
    }

    @Test
    @DisplayName("A white spell on the stack is a legal source choice")
    void whiteSpellOnStackIsLegalSourceChoice() {
        addCreatureReady(player1, new CircleOfProtectionWhite());
        HealingSalve healingSalve = new HealingSalve();
        harness.setHand(player2, List.of(healingSalve));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player2, 0, 0, player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(healingSalve.getId());
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        addCreatureReady(player1, new CircleOfProtectionWhite());
        Permanent soldier = addCreatureReady(player2, new PearledUnicorn());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, soldier.getId());

        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    void damageToAnotherPlayerDoesNotConsumeShield() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new CircleOfProtectionWhite());
        Permanent source = addCreatureReady(player1, new PearledUnicorn());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());
        source.setAttacking(true);
        resolveCombat(player1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId()) && s.sourceId().equals(source.getId()));
    }

}
