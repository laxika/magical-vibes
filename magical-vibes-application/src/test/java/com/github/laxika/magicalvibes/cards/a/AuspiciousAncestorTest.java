package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AuspiciousAncestor.class, FemerefScouts.class, FeralShadow.class, Incinerate.class})
class AuspiciousAncestorTest extends BaseCardTest {

    private void setUpOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("When Auspicious Ancestor dies, its controller gains 3 life")
    void diesGainsThreeLife() {
        harness.addToBattlefield(player1, new AuspiciousAncestor());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Auspicious Ancestor"));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 3);
    }

    @Test
    @DisplayName("Opponent's white spell: paying {1} gains 1 life")
    void payGainsOneLife() {
        harness.addToBattlefield(player1, new AuspiciousAncestor());
        setUpOpponentTurn();
        int startingLife = gd.playerLifeTotals.get(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player2, new FemerefScouts(), "{2}{W}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Declining to pay {1} gains no life")
    void declineGainsNoLife() {
        harness.addToBattlefield(player1, new AuspiciousAncestor());
        setUpOpponentTurn();
        int startingLife = gd.playerLifeTotals.get(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player2, new FemerefScouts(), "{2}{W}");
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("A nonwhite spell does not trigger the ability")
    void nonwhiteSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new AuspiciousAncestor());
        setUpOpponentTurn();
        harness.castFromHand(player2, new FeralShadow(), "{2}{B}");

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller's own white spell triggers the ability too")
    void ownWhiteSpellTriggers() {
        harness.addToBattlefield(player1, new AuspiciousAncestor());
        int startingLife = gd.playerLifeTotals.get(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new FemerefScouts(), "{2}{W}");
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 1);
    }

    @Test
    @DisplayName("Accepting without {1} gains no life")
    void cannotPayGainsNoLife() {
        harness.addToBattlefield(player1, new AuspiciousAncestor());
        setUpOpponentTurn();
        int startingLife = gd.playerLifeTotals.get(player1.getId());
        harness.castFromHand(player2, new FemerefScouts(), "{2}{W}");

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }
}
