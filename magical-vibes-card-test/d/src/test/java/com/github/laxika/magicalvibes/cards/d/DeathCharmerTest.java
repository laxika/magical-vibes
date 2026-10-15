package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.MarshBoa;
import com.github.laxika.magicalvibes.cards.r.RibCageSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathCharmer.class, RibCageSpider.class, MarshBoa.class})
class DeathCharmerTest extends BaseCardTest {

    @Test
    @DisplayName("The damaged creature's controller loses 2 life when they decline to pay")
    void losesLifeWhenDamagedCreatureControllerDeclinesToPay() {
        Permanent deathCharmer = addCreatureReady(player1, new DeathCharmer());
        deathCharmer.setAttacking(true);
        addCreatureReady(player2, new RibCageSpider());

        resolveCombatToPaymentChoice();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The damaged creature's controller may pay {2} to prevent losing life")
    void payingPreventsLifeLoss() {
        Permanent deathCharmer = addCreatureReady(player1, new DeathCharmer());
        deathCharmer.setAttacking(true);
        addCreatureReady(player2, new RibCageSpider());

        resolveCombatToPaymentChoice();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Still triggers when the damaged creature dies in combat")
    void triggersWhenDamagedCreatureDiesInCombat() {
        Permanent deathCharmer = addCreatureReady(player1, new DeathCharmer());
        deathCharmer.setAttacking(true);
        addCreatureReady(player2, new MarshBoa());

        resolveCombatToPaymentChoice();

        harness.assertInGraveyard(player2, "Marsh Boa");
        PendingInteraction.MayAbilityChoice paymentChoice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(paymentChoice).isNotNull();
        assertThat(paymentChoice.playerId()).isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Combat damage to a player does not trigger Death Charmer")
    void combatDamageToPlayerDoesNotTrigger() {
        Permanent deathCharmer = addCreatureReady(player1, new DeathCharmer());
        deathCharmer.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The damaged creature's controller can pay even after that creature dies")
    void canPayAfterDamagedCreatureDies() {
        Permanent deathCharmer = addCreatureReady(player1, new DeathCharmer());
        deathCharmer.setAttacking(true);
        addCreatureReady(player2, new MarshBoa());

        resolveCombatToPaymentChoice();

        harness.assertInGraveyard(player2, "Marsh Boa");
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Death Charmer triggers when blocking and dying in combat")
    void triggersAsBlockerEvenWhenItDies() {
        Permanent attacker = addCreatureReady(player1, new DeathCharmer());
        attacker.setAttacking(true);
        addCreatureReady(player2, new DeathCharmer());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Death Charmer");
        harness.assertInGraveyard(player2, "Death Charmer");
        PendingInteraction.MayAbilityChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(firstChoice).isNotNull();
        harness.handleMayAbilityChosen(
                firstChoice.playerId().equals(player1.getId()) ? player1 : player2, false);
        harness.passBothPriorities();
        PendingInteraction.MayAbilityChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isNotEqualTo(firstChoice.playerId());
        harness.handleMayAbilityChosen(
                secondChoice.playerId().equals(player1.getId()) ? player1 : player2, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private void resolveCombatToPaymentChoice() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
