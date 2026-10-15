package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarksteelPlate;
import com.github.laxika.magicalvibes.cards.p.Phyresis;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
@CardUsed({CosmicHorror.class, DarksteelPlate.class, Phyresis.class, CircleOfProtectionBlack.class})

class CosmicHorrorTest extends BaseCardTest {

    @Test
    @DisplayName("Declining to pay destroys Cosmic Horror and deals 7 damage to its controller")
    void declineDestroysAndDamages() {
        harness.addToBattlefield(player1, new CosmicHorror());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 7);
    }

    @Test
    @DisplayName("Paying {3}{B}{B}{B} keeps it on the battlefield and deals no damage")
    void payKeepsItAlive() {
        harness.addToBattlefield(player1, new CosmicHorror());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may-pay prompt
        harness.addMana(player1, ManaColor.BLACK, 6); // mana empties between steps — add it at payment time
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("The generic portion of the upkeep cost can be paid with other colors")
    void paysWithMixedMana() {
        harness.addToBattlefield(player1, new CosmicHorror());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Six mana with fewer than three black cannot pay the upkeep cost")
    void insufficientBlackManaDestroysAndDamages() {
        harness.addToBattlefield(player1, new CosmicHorror());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.UPKEEP, () -> {
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
            assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 7);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(4);
        });
    }

    @Test
    @DisplayName("An opponent's Cosmic Horror damages that opponent on their upkeep")
    void opponentControlledHorrorDamagesOpponent() {
        harness.addToBattlefield(player2, new CosmicHorror());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        int otherLifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 7);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(otherLifeBefore);
    }

    @Test
    @DisplayName("Accepting without enough mana still destroys it and deals 7 damage")
    void acceptWithoutManaDestroysAndDamages() {
        harness.addToBattlefield(player1, new CosmicHorror());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 5); // one short of {3}{B}{B}{B}
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 7);
    }

    @Test
    @DisplayName("A regenerated Cosmic Horror survives and deals no damage (only 'destroyed this way' triggers it)")
    void regeneratedSurvivesWithoutDamage() {
        Permanent horror = harness.addToBattlefieldAndReturn(player1, new CosmicHorror());
        horror.setRegenerationShield(1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false); // decline → destruction is replaced by regeneration

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @CardUsed({CosmicHorror.class, DarksteelPlate.class})
    @DisplayName("An indestructible Cosmic Horror survives without dealing damage")
    void indestructibleSurvivesWithoutDamage() {
        Permanent horror = harness.addToBattlefieldAndReturn(player1, new CosmicHorror());
        Permanent plate = harness.addToBattlefieldAndReturn(player1, new DarksteelPlate());
        plate.setAttachedTo(horror.getId());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new CosmicHorror());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("If Cosmic Horror leaves before its trigger resolves, it deals no damage")
    void sourceLeavingBeforeResolutionPreventsDamage() {
        Permanent horror = harness.addToBattlefieldAndReturn(player1, new CosmicHorror());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, horror));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @CardUsed({CosmicHorror.class, Phyresis.class})
    @DisplayName("A destroyed Horror with infect gives seven poison counters instead of causing life loss")
    void infectAppliesToDamageAfterDestruction() {
        Permanent horror = harness.addToBattlefieldAndReturn(player1, new CosmicHorror());
        Permanent phyresis = harness.addToBattlefieldAndReturn(player1, new Phyresis());
        phyresis.setAttachedTo(horror.getId());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Cosmic Horror");
        harness.assertLife(player1, lifeBefore);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(7);
    }

    @Test
    @CardUsed({CosmicHorror.class, CircleOfProtectionBlack.class})
    @DisplayName("Circle of Protection Black prevents upkeep damage from the chosen Horror after it dies")
    void chosenSourcePreventionAppliesAfterDestruction() {
        harness.addToBattlefield(player1, new CircleOfProtectionBlack());
        Permanent horror = harness.addToBattlefieldAndReturn(player1, new CosmicHorror());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, horror.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Cosmic Horror");
        harness.assertOnBattlefield(player1, "Circle of Protection: Black");
        harness.assertLife(player1, lifeBefore);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }
}
