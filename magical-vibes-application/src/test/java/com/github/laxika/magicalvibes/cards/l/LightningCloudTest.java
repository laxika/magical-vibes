package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FirestormHellkite;
import com.github.laxika.magicalvibes.cards.i.InfantryVeteran;
import com.github.laxika.magicalvibes.cards.s.SpittingDrake;
import com.github.laxika.magicalvibes.cards.w.Warthog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightningCloud.class, SpittingDrake.class, InfantryVeteran.class, Warthog.class,
        FirestormHellkite.class})
class LightningCloudTest extends BaseCardTest {

    private void setUpOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Opponent's red spell: paying {R} deals 1 damage to the chosen player")
    void payDealsDamageToPlayer() {
        harness.addToBattlefield(player1, new LightningCloud());
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castFromHand(player2, new SpittingDrake(), "{3}{R}");

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Paying {R} can deal 1 damage to a creature")
    void payDealsDamageToCreature() {
        harness.addToBattlefield(player1, new LightningCloud());
        harness.addToBattlefield(player2, new InfantryVeteran());
        UUID veteranId = harness.getPermanentId(player2, "Infantry Veteran");
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromHand(player2, new SpittingDrake(), "{3}{R}");
        harness.handlePermanentChosen(player1, veteranId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Infantry Veteran");
    }

    @Test
    @DisplayName("Declining to pay {R} deals no damage")
    void declineDealsNoDamage() {
        harness.addToBattlefield(player1, new LightningCloud());
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castFromHand(player2, new SpittingDrake(), "{3}{R}");
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Controller's own red spell triggers too")
    void ownRedSpellTriggers() {
        harness.addToBattlefield(player1, new LightningCloud());

        harness.castFromHand(player1, new SpittingDrake(), "{3}{R}");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("A multicolored red spell triggers too")
    void multicoloredRedSpellTriggers() {
        harness.addToBattlefield(player1, new LightningCloud());
        setUpOpponentTurn();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castFromHand(player2, new FirestormHellkite(), "{4}{U}{R}");

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Accepting without available red mana deals no damage")
    void cannotPayNoMana() {
        harness.addToBattlefield(player1, new LightningCloud());
        setUpOpponentTurn();
        harness.setLife(player2, 20);

        harness.castFromHand(player2, new SpittingDrake(), "{3}{R}");

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A nonred spell does not trigger")
    void nonRedDoesNotTrigger() {
        harness.addToBattlefield(player1, new LightningCloud());
        setUpOpponentTurn();

        harness.castFromHand(player2, new Warthog(), "{1}{G}{G}");

        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Lightning Cloud"));
    }
}
