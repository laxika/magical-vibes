package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SoulCharmer.class, RibCageSpider.class, MarshBoa.class})
class SoulCharmerTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life when the damaged creature's controller declines to pay")
    void gainsLifeWhenDamagedCreatureControllerDeclinesToPay() {
        harness.setLife(player1, 10);
        Permanent soulCharmer = addCreatureReady(player1, new SoulCharmer());
        soulCharmer.setAttacking(true);
        addCreatureReady(player2, new RibCageSpider());

        resolveCombatToPaymentChoice();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("The damaged creature's controller may pay {2} to prevent the life gain")
    void payingPreventsLifeGain() {
        harness.setLife(player1, 10);
        Permanent soulCharmer = addCreatureReady(player1, new SoulCharmer());
        soulCharmer.setAttacking(true);
        addCreatureReady(player2, new RibCageSpider());

        resolveCombatToPaymentChoice();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Still triggers when the damaged creature dies in combat")
    void triggersWhenDamagedCreatureDiesInCombat() {
        harness.setLife(player1, 10);
        Permanent soulCharmer = addCreatureReady(player1, new SoulCharmer());
        soulCharmer.setAttacking(true);
        addCreatureReady(player2, new MarshBoa());

        resolveCombatToPaymentChoice();

        harness.assertInGraveyard(player2, "Marsh Boa");
        PendingInteraction.MayAbilityChoice paymentChoice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(paymentChoice).isNotNull();
        assertThat(paymentChoice.playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Combat damage to a player does not trigger Soul Charmer")
    void combatDamageToPlayerDoesNotTrigger() {
        harness.setLife(player1, 10);
        Permanent soulCharmer = addCreatureReady(player1, new SoulCharmer());
        soulCharmer.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The controller of a creature that died in combat can still pay")
    void payingAfterDamagedCreatureDiesPreventsLifeGain() {
        harness.setLife(player1, 10);
        Permanent soulCharmer = addCreatureReady(player1, new SoulCharmer());
        soulCharmer.setAttacking(true);
        addCreatureReady(player2, new MarshBoa());

        resolveCombatToPaymentChoice();

        harness.assertInGraveyard(player2, "Marsh Boa");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Soul Charmer also triggers when it deals combat damage as a blocker")
    void blockingTriggersAndGainsLifeForDefender() {
        harness.setLife(player2, 10);
        Permanent attacker = addCreatureReady(player1, new RibCageSpider());
        attacker.setAttacking(true);
        addCreatureReady(player2, new SoulCharmer());

        resolveCombatToPaymentChoice();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("An unsuccessful payment does not prevent the life gain")
    void insufficientManaDoesNotPreventLifeGain() {
        harness.setLife(player1, 10);
        Permanent soulCharmer = addCreatureReady(player1, new SoulCharmer());
        soulCharmer.setAttacking(true);
        addCreatureReady(player2, new RibCageSpider());

        resolveCombatToPaymentChoice();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Both Soul Charmers still grant life when they die dealing combat damage to each other")
    void bothDyingSoulCharmersTriggerIndependently() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        Permanent attacker = addCreatureReady(player1, new SoulCharmer());
        attacker.setAttacking(true);
        addCreatureReady(player2, new SoulCharmer());

        resolveCombatToPaymentChoice();

        harness.assertInGraveyard(player1, "Soul Charmer");
        harness.assertInGraveyard(player2, "Soul Charmer");
        PendingInteraction.MayAbilityChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(firstChoice).isNotNull();
        harness.handleMayAbilityChosen(firstChoice.playerId().equals(player1.getId()) ? player1 : player2, false);
        resolveAllTriggers();

        PendingInteraction.MayAbilityChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isNotEqualTo(firstChoice.playerId());
        harness.handleMayAbilityChosen(secondChoice.playerId().equals(player1.getId()) ? player1 : player2, false);

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 12);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void resolveCombatToPaymentChoice() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
