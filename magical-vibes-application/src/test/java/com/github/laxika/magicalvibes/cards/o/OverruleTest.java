package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AzoriusFirstWing;
import com.github.laxika.magicalvibes.cards.d.Demonfire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Overrule.class, AzoriusFirstWing.class, Demonfire.class})
class OverruleTest extends BaseCardTest {

    @Test
    void countersTargetSpellAndGainsXLifeWhenItsControllerCannotPay() {
        AzoriusFirstWing firstWing = new AzoriusFirstWing();

        harness.setHand(player2, List.of(new Overrule()));
        addOverruleMana(player2, 2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player1, firstWing, "{W}{U}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 2, firstWing.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Azorius First-Wing");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingXKeepsTargetSpellAndStillGainsXLife() {
        AzoriusFirstWing firstWing = new AzoriusFirstWing();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new Overrule()));
        addOverruleMana(player2, 2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player1, firstWing, "{W}{U}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 2, firstWing.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore + 2);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Azorius First-Wing");
    }

    @Test
    void xZeroCanBePaidForFreeAndDoesNotGainLife() {
        AzoriusFirstWing firstWing = new AzoriusFirstWing();

        harness.setHand(player2, List.of(new Overrule()));
        addOverruleMana(player2, 0);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player1, firstWing, "{W}{U}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, firstWing.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Azorius First-Wing");
    }

    @Test
    void stillGainsXLifeWhenTargetSpellCannotBeCountered() {
        Demonfire demonfire = new Demonfire();
        harness.setHand(player1, List.of(demonfire));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new Overrule()));
        addOverruleMana(player2, 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, demonfire.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player2, "Overrule");

        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        harness.assertInGraveyard(player1, "Demonfire");
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new Overrule()));
        addOverruleMana(player1, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void decliningAffordablePaymentCountersSpellAndStillGainsLife() {
        AzoriusFirstWing firstWing = new AzoriusFirstWing();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromHand(player1, firstWing, "{W}{U}");
        harness.setHand(player2, List.of(new Overrule()));
        addOverruleMana(player2, 2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.passPriority(player1);
        harness.castInstant(player2, 0, 2, firstWing.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Azorius First-Wing");
        harness.assertLife(player2, lifeBefore + 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotGainLifeWhenTargetHasAlreadyLeftTheStack() {
        AzoriusFirstWing firstWing = new AzoriusFirstWing();
        harness.castFromHand(player1, firstWing, "{W}{U}");
        harness.setHand(player2, List.of(new Overrule(), new Overrule()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.passPriority(player1);
        harness.castInstant(player2, 0, 3, firstWing.getId());
        harness.castInstant(player2, 0, 1, firstWing.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Azorius First-Wing");
        harness.assertLife(player2, lifeBefore + 1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore + 1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    private void addOverruleMana(com.github.laxika.magicalvibes.model.Player player, int xValue) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, xValue);
    }
}
