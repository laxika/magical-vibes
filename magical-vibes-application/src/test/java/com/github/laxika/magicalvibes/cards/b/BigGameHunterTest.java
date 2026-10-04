package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvenRiftwatcher;
import com.github.laxika.magicalvibes.cards.g.Groundbreaker;
import com.github.laxika.magicalvibes.cards.r.RiptidePilferer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BigGameHunter.class, Groundbreaker.class, AvenRiftwatcher.class, RiptidePilferer.class})
class BigGameHunterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys a target creature with power 4 or greater and prevents regeneration")
    void etbDestroysHighPowerCreatureWithoutRegeneration() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Groundbreaker());
        target.setRegenerationShield(1);
        harness.setHand(player1, List.of(new BigGameHunter()));
        addBigGameHunterMana(player1);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Groundbreaker");
        harness.assertInGraveyard(player2, "Groundbreaker");
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 4")
    void cannotTargetLowPowerCreature() {
        harness.addToBattlefield(player2, new AvenRiftwatcher());
        harness.setHand(player1, List.of(new BigGameHunter()));
        addBigGameHunterMana(player1);

        assertThatThrownBy(() -> harness.castCreature(
                player1, 0, harness.getPermanentId(player2, "Aven Riftwatcher")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("Madness casts Big Game Hunter for {B}")
    void madnessCastsBigGameHunter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Groundbreaker());
        discardBigGameHunterViaRiptidePilferer();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Groundbreaker");
        harness.assertOnBattlefield(player1, "Big Game Hunter");
    }

    @Test
    @DisplayName("Declining madness puts Big Game Hunter into the graveyard")
    void decliningMadnessPutsBigGameHunterIntoGraveyard() {
        BigGameHunter hunter = discardBigGameHunterViaRiptidePilferer();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(hunter.getId()));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Big Game Hunter");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(hunter.getId()));
    }

    @Test
    @DisplayName("ETB can destroy a creature its controller owns with exactly four power")
    void destroysOwnCreatureWithExactlyFourPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Groundbreaker());
        target.setPowerModifier(-2);
        harness.setHand(player1, List.of(new BigGameHunter()));
        addBigGameHunterMana(player1);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Groundbreaker");
        harness.assertOnBattlefield(player1, "Big Game Hunter");
    }

    @Test
    @DisplayName("ETB does not destroy a target whose power falls below four before resolution")
    void targetPowerIsRecheckedOnResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Groundbreaker());
        harness.setHand(player1, List.of(new BigGameHunter()));
        addBigGameHunterMana(player1);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        target.setPowerModifier(-3);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Groundbreaker");
        harness.assertNotInGraveyard(player2, "Groundbreaker");
        harness.assertOnBattlefield(player1, "Big Game Hunter");
    }

    @Test
    @DisplayName("Big Game Hunter can be cast when no creature has four power")
    void canBeCastWithoutAnEtbTarget() {
        harness.castFromHand(player1, new BigGameHunter(), "{1}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Big Game Hunter");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Madness can cast Big Game Hunter without a legal ETB target")
    void madnessCanBeCastWithoutAnEtbTarget() {
        discardBigGameHunterViaRiptidePilferer();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Big Game Hunter");
        harness.assertNotInGraveyard(player1, "Big Game Hunter");
    }

    @Test
    @DisplayName("An unpaid madness cast puts the discarded card into the graveyard")
    void unpaidMadnessCastDoesNotLeaveCardInExile() {
        BigGameHunter hunter = discardBigGameHunterViaRiptidePilferer();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Big Game Hunter");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(hunter.getId()));
    }

    private BigGameHunter discardBigGameHunterViaRiptidePilferer() {
        BigGameHunter hunter = new BigGameHunter();
        harness.setHand(player1, List.of(hunter));
        Permanent pilferer = addCreatureReady(player2, new RiptidePilferer());
        pilferer.setAttacking(true);
        resolveCombat(player2);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        return hunter;
    }

    private void addBigGameHunterMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.addMana(player, ManaColor.COLORLESS, 1);
    }
}
