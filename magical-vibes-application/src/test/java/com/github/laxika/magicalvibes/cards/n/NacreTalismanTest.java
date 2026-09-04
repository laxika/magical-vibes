package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.e.EnergyStorm;
import com.github.laxika.magicalvibes.cards.k.KjeldoranWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NacreTalisman.class, BalduvianBears.class, KjeldoranWarrior.class, EnergyStorm.class})
class NacreTalismanTest extends BaseCardTest {

    private void setUpOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent addTappedBears(Player owner) {
        Permanent bears = addCreatureReady(owner, new BalduvianBears());
        bears.tap();
        return bears;
    }

    @Test
    @DisplayName("Opponent's white spell: paying {3} untaps the chosen permanent")
    void payUntapsTarget() {
        harness.addToBattlefield(player1, new NacreTalisman());
        Permanent bears = addTappedBears(player1);
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromHand(player2, new KjeldoranWarrior(), "{W}");

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).contains(bears.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Declining to pay {3} leaves the permanent tapped")
    void declineLeavesTapped() {
        harness.addToBattlefield(player1, new NacreTalisman());
        Permanent bears = addTappedBears(player1);
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromHand(player2, new KjeldoranWarrior(), "{W}");

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller's own white spell triggers too — any player casting counts")
    void ownWhiteSpellTriggers() {
        harness.addToBattlefield(player1, new NacreTalisman());
        Permanent bears = addTappedBears(player1);
        harness.castFromHand(player1, new KjeldoranWarrior(), "{W}");

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.playerId()).isEqualTo(player1.getId());
        assertThat(targetChoice.validIds()).contains(bears.getId());

    }

    @Test
    @DisplayName("A nonwhite spell does not trigger")
    void nonWhiteDoesNotTrigger() {
        harness.addToBattlefield(player1, new NacreTalisman());
        addTappedBears(player1);
        setUpOpponentTurn();
        harness.castFromHand(player2, new BalduvianBears(), "{1}{G}");

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Nacre Talisman"));
    }

    @Test
    @DisplayName("Any permanent may be targeted, including one an opponent controls")
    void untapsOpponentPermanent() {
        harness.addToBattlefield(player1, new NacreTalisman());
        Permanent bears = addTappedBears(player2);
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromHand(player2, new KjeldoranWarrior(), "{W}");

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A white noncreature spell triggers the Talisman")
    void whiteNoncreatureSpellTriggers() {
        harness.addToBattlefield(player1, new NacreTalisman());
        Permanent bears = addTappedBears(player1);
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromHand(player2, new EnergyStorm(), "{1}{W}");

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).contains(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The Talisman may target a noncreature permanent")
    void untapsNoncreaturePermanent() {
        Permanent talisman = harness.addToBattlefieldAndReturn(player1, new NacreTalisman());
        talisman.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromHand(player1, new KjeldoranWarrior(), "{W}");

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).contains(talisman.getId());
        harness.handlePermanentChosen(player1, talisman.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(talisman.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Accepting without three mana does not untap the target")
    void cannotPayLeavesTargetTapped() {
        harness.addToBattlefield(player1, new NacreTalisman());
        Permanent bears = addTappedBears(player1);
        setUpOpponentTurn();
        harness.castFromHand(player2, new KjeldoranWarrior(), "{W}");

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isTrue();
    }
}
