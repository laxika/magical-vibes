package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Glaciers.class, Mountain.class, Plains.class})
class GlaciersTest extends BaseCardTest {

    @Test
    @DisplayName("A Mountain taps for white instead of red")
    void mountainProducesWhite() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Glaciers());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("Also converts a Mountain the opponent controls")
    void convertsOpponentMountain() {
        harness.addToBattlefield(player1, new Glaciers());
        harness.addToBattlefield(player2, new Mountain());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("A Plains is unaffected — still taps for white")
    void plainsUnaffected() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Glaciers());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A Mountain taps for red again once Glaciers leaves")
    void redResumesWhenGlaciersLeaves() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Glaciers());
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Glaciers"));
        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Declining to pay {W}{U} sacrifices Glaciers")
    void decliningPaymentSacrifices() {
        harness.addToBattlefield(player1, new Glaciers());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Glaciers");
        harness.assertInGraveyard(player1, "Glaciers");
    }

    @Test
    @DisplayName("Paying {W}{U} keeps Glaciers on the battlefield")
    void payingKeepsEnchantment() {
        harness.addToBattlefield(player1, new Glaciers());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Glaciers");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Mountains entering after Glaciers are Plains immediately")
    void convertsNewMountainAndReplacesItsLandType() {
        harness.addToBattlefield(player1, new Glaciers());
        harness.addToBattlefield(player1, new Mountain());
        Permanent mountain = findPermanent(player1, "Mountain");

        assertThat(gqs.hasEffectiveSubtype(gd, mountain, CardSubtype.PLAINS)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, mountain, CardSubtype.MOUNTAIN)).isFalse();

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Glaciers does not demand payment on an opponent's upkeep")
    void opponentUpkeepDoesNotTrigger() {
        harness.addToBattlefield(player1, new Glaciers());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Glaciers");
        harness.assertNotInGraveyard(player1, "Glaciers");
    }

    @Test
    @DisplayName("Two white mana cannot pay the white and blue upkeep cost")
    void missingBlueManaSacrificesWithoutPartialPayment() {
        harness.addToBattlefield(player1, new Glaciers());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Glaciers");
        harness.assertInGraveyard(player1, "Glaciers");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing Glaciers for unpaid upkeep restores Mountain mana")
    void unpaidUpkeepRestoresMountainMana() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Glaciers());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.tapPermanent(player1, 0);

        harness.assertInGraveyard(player1, "Glaciers");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }
}
