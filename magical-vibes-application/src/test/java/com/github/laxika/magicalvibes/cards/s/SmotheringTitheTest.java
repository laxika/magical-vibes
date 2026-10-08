package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mortify;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmotheringTithe.class, SphinxsInsight.class, Mortify.class})
class SmotheringTitheTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }

    @Test
    @DisplayName("Opponent declines to pay and creates a Treasure token for Smothering Tithe's controller")
    void decliningToPayCreatesTreasure() {
        harness.addToBattlefield(player1, new SmotheringTithe());

        advanceToDraw(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Opponent pays {2} and no Treasure token is created")
    void payingPreventsTreasure() {
        harness.addToBattlefield(player1, new SmotheringTithe());

        advanceToDraw(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Smothering Tithe does not trigger on its controller's draw")
    void doesNotTriggerOnControllerDraw() {
        harness.addToBattlefield(player1, new SmotheringTithe());

        advanceToDraw(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void eachCardDrawnRequiresASeparatePayment() {
        harness.addToBattlefield(player1, new SmotheringTithe());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new SphinxsInsight()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void twoTithesRequireIndependentPaymentsForOneDraw() {
        harness.addToBattlefield(player1, new SmotheringTithe());
        harness.addToBattlefield(player1, new SmotheringTithe());
        advanceToDraw(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerStillCreatesTreasureAfterTitheIsDestroyed() {
        harness.addToBattlefield(player1, new SmotheringTithe());
        advanceToDraw(player2);
        harness.setHand(player1, List.of(new Mortify()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Smothering Tithe"));
        harness.assertNotOnBattlefield(player1, "Smothering Tithe");
        harness.assertInGraveyard(player1, "Smothering Tithe");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void createdTreasureCanBeSacrificedForManaImmediately() {
        harness.addToBattlefield(player1, new SmotheringTithe());
        advanceToDraw(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Treasure")), null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
