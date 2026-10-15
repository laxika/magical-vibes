package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DrossHopper;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.m.MoriokReaver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FurnaceCelebration.class, DrossHopper.class, MoriokReaver.class, Memnite.class,
        Ferrovore.class, LiquimetalCoating.class})
class FurnaceCelebrationTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature requires a target before the payment choice")
    void sacrificeTriggersMayPrompt() {
        sacrificeCreatureAndChooseTarget(player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting pays {2} and immediately deals 2 damage to the chosen creature")
    void acceptPaysDamageToCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Memnite());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        sacrificeCreatureAndChooseTarget(target.getId());
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Memnite");
        harness.assertInGraveyard(player2, "Memnite");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting pays {2} and deals exactly 2 damage to the chosen player")
    void acceptPaysDamageToPlayer() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        sacrificeCreatureAndChooseTarget(player2.getId());
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller can choose themselves as the damage target")
    void canDamageController() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        sacrificeCreatureAndChooseTarget(player1.getId());
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining does not deal damage or spend mana")
    void declineDoesNothing() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        sacrificeCreatureAndChooseTarget(player2.getId());
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, false);

        assertNoCelebrationTrigger();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Accepting with insufficient mana does not deal damage or spend partial mana")
    void cannotPayTreatsAsDecline() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        sacrificeCreatureAndChooseTarget(player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertNoCelebrationTrigger();
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's sacrifice does not trigger your Furnace Celebration")
    void opponentSacrificeDoesNotTrigger() {
        harness.addToBattlefield(player1, new FurnaceCelebration());
        harness.addToBattlefield(player2, new DrossHopper());
        Permanent reaver = harness.addToBattlefieldAndReturn(player2, new MoriokReaver());

        harness.activateAbility(player2, 0, null, null);
        harness.handlePermanentChosen(player2, reaver.getId());

        assertNoCelebrationTrigger();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Moriok Reaver");
    }

    @Test
    @DisplayName("Sacrificing Furnace Celebration itself does not trigger its ability")
    void sacrificingItselfDoesNotTrigger() {
        Permanent celebration = harness.addToBattlefieldAndReturn(player1, new FurnaceCelebration());
        harness.addToBattlefield(player1, new Ferrovore());
        harness.addToBattlefield(player1, new LiquimetalCoating());
        harness.activateAbility(player1, 2, null, celebration.getId());
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, celebration.getId());

        harness.assertInGraveyard(player1, "Furnace Celebration");
        assertNoCelebrationTrigger();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificing a noncreature artifact also triggers Furnace Celebration")
    void noncreatureSacrificeTriggers() {
        harness.addToBattlefield(player1, new FurnaceCelebration());
        harness.addToBattlefield(player1, new Ferrovore());
        Permanent coating = harness.addToBattlefieldAndReturn(player1, new LiquimetalCoating());
        harness.addToBattlefield(player1, new Memnite());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, coating.getId());
        chooseCelebrationTarget(player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Liquimetal Coating");
        harness.assertLife(player2, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A target sacrificed in response makes the trigger resolve without a payment choice")
    void removedTargetDoesNotAllowPayment() {
        harness.addToBattlefield(player2, new DrossHopper());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Memnite());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        sacrificeCreatureAndChooseTarget(target.getId());

        harness.activateAbility(player2, 0, null, null);
        harness.handlePermanentChosen(player2, target.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertNoCelebrationTrigger();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Memnite");
    }

    private void sacrificeCreatureAndChooseTarget(UUID targetId) {
        harness.addToBattlefield(player1, new FurnaceCelebration());
        harness.addToBattlefield(player1, new DrossHopper());
        Permanent reaver = harness.addToBattlefieldAndReturn(player1, new MoriokReaver());
        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, reaver.getId());
        chooseCelebrationTarget(targetId);
    }

    private void chooseCelebrationTarget(UUID targetId) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.handlePermanentChosen(player1, targetId);
    }

    private void assertNoCelebrationTrigger() {
        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Furnace Celebration"));
    }
}
