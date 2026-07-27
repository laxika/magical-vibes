package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConversionTest extends BaseCardTest {

    // ===== Static: All Mountains are Plains =====

    @Test
    @DisplayName("A Mountain taps for white instead of red")
    void mountainProducesWhite() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Conversion());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("A Plains is unaffected — still taps for white")
    void plainsUnaffected() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Conversion());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Also converts a Mountain the opponent controls")
    void convertsOpponentMountain() {
        harness.addToBattlefield(player2, new Mountain());
        Permanent mountain = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.addToBattlefield(player1, new Conversion());

        com.github.laxika.magicalvibes.service.battlefield.GameQueryService.StaticBonus bonus =
                gqs.computeStaticBonus(gd, mountain);

        assertThat(bonus.subtypeOverriding()).isTrue();
        assertThat(bonus.landSubtypeOverriding()).isTrue();
        assertThat(bonus.grantedSubtypes())
                .containsExactly(com.github.laxika.magicalvibes.model.CardSubtype.PLAINS);
    }

    @Test
    @DisplayName("A Mountain taps for red again once Conversion leaves")
    void redResumesWhenConversionLeaves() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Conversion());
        Permanent conversion = gd.playerBattlefields.get(player1.getId()).get(1);

        gd.playerBattlefields.get(player1.getId()).remove(conversion);
        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(0);
    }

    // ===== Upkeep sacrifice-unless-pay {W}{W} =====

    @Test
    @DisplayName("Declining to pay {W}{W} sacrifices Conversion")
    void decliningPaymentSacrifices() {
        harness.addToBattlefield(player1, new Conversion());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Conversion");
        harness.assertInGraveyard(player1, "Conversion");
    }

    @Test
    @DisplayName("Paying {W}{W} keeps Conversion on the battlefield")
    void payingKeepsEnchantment() {
        harness.addToBattlefield(player1, new Conversion());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Conversion");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }
}
