package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BasilicaScreecher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KingpinsPet.class, BasilicaScreecher.class})
class KingpinsPetTest extends BaseCardTest {

    @Test
    @DisplayName("Paying Extort drains the opponent and gains life")
    void payingExtortDrainsOpponent() {
        harness.addToBattlefield(player1, new KingpinsPet());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromHand(player1, new BasilicaScreecher(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Declining Extort does nothing")
    void decliningExtortDoesNothing() {
        harness.addToBattlefield(player1, new KingpinsPet());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromHand(player1, new BasilicaScreecher(), "{1}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Kingpin's Pet does not trigger for an opponent's spell")
    void opponentSpellDoesNotTriggerExtort() {
        harness.addToBattlefield(player1, new KingpinsPet());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new BasilicaScreecher(), "{1}{B}");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Extort payment is offered only when the trigger resolves")
    void paymentWaitsForTriggerResolution() {
        harness.addToBattlefield(player1, new KingpinsPet());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromHand(player1, new BasilicaScreecher(), "{1}{B}");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Extort still resolves after Kingpin's Pet leaves the battlefield")
    void triggerSurvivesSourceLeavingBattlefield() {
        harness.addToBattlefield(player1, new KingpinsPet());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromHand(player1, new BasilicaScreecher(), "{1}{B}");
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Casting Kingpin's Pet does not trigger its own extort")
    void doesNotTriggerForItsOwnCast() {
        harness.castFromHand(player1, new KingpinsPet(), "{1}{W}{B}");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
