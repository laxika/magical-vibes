package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EkunduGriffin;
import com.github.laxika.magicalvibes.cards.g.GrinningTotem;
import com.github.laxika.magicalvibes.cards.l.LightningSerpent;
import com.github.laxika.magicalvibes.cards.p.PorcelainLegionnaire;
import com.github.laxika.magicalvibes.cards.v.VenerableMonk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Flash.class, EkunduGriffin.class, GrinningTotem.class, LightningSerpent.class,
        PorcelainLegionnaire.class, VenerableMonk.class})
class FlashTest extends BaseCardTest {

    private void castFlash() {
        // Flash costs {1}{U}.
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Paying the reduced cost keeps the creature on the battlefield")
    void payingReducedCostKeepsCreature() {
        harness.setHand(player1, List.of(new Flash(), new EkunduGriffin()));
        castFlash();

        // Put Ekundu Griffin (index 0 now that Flash has left the hand) onto the battlefield.
        harness.handleCardChosen(player1, 0);

        // Ekundu Griffin is {3}{W}; reduced by {2} the cost to keep it is {1}{W}.
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Ekundu Griffin");
        harness.assertNotInGraveyard(player1, "Ekundu Griffin");
    }

    @Test
    @DisplayName("Declining to pay sacrifices the creature")
    void decliningToPaySacrificesCreature() {
        harness.setHand(player1, List.of(new Flash(), new EkunduGriffin()));
        castFlash();

        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Ekundu Griffin");
        harness.assertInGraveyard(player1, "Ekundu Griffin");
    }

    @Test
    @DisplayName("Accepting without enough mana still sacrifices the creature")
    void cannotPaySacrificesCreature() {
        harness.setHand(player1, List.of(new Flash(), new EkunduGriffin()));
        castFlash();

        harness.handleCardChosen(player1, 0);
        // No mana is available - the reduced {1}{W} cost cannot be paid.
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Ekundu Griffin");
        harness.assertInGraveyard(player1, "Ekundu Griffin");
    }

    @Test
    @DisplayName("Declining to put a creature leaves it in hand")
    void decliningToPutLeavesCreatureInHand() {
        harness.setHand(player1, List.of(new Flash(), new EkunduGriffin()));
        castFlash();

        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Ekundu Griffin");
        harness.assertInHand(player1, "Ekundu Griffin");
    }

    @Test
    @DisplayName("With no creature in hand the spell resolves with no choice")
    void noCreatureInHandResolvesHarmlessly() {
        harness.setHand(player1, List.of(new Flash(), new GrinningTotem()));
        castFlash();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Grinning Totem");
    }

    @Test
    @DisplayName("Only creature cards are offered from a mixed hand")
    void mixedHandOffersOnlyCreatureCards() {
        harness.setHand(player1, List.of(new Flash(), new GrinningTotem(), new EkunduGriffin()));
        castFlash();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
        harness.handleCardChosen(player1, 1);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Grinning Totem");
        harness.assertInGraveyard(player1, "Ekundu Griffin");
    }

    @Test
    @DisplayName("Only generic mana is reduced from the creature's mana cost")
    void coloredManaIsStillRequired() {
        harness.setHand(player1, List.of(new Flash(), new EkunduGriffin()));
        castFlash();

        harness.handleCardChosen(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Ekundu Griffin");
        harness.assertInGraveyard(player1, "Ekundu Griffin");
    }

    @Test
    @DisplayName("The creature's enters-the-battlefield ability triggers before it is sacrificed")
    void entersBattlefieldAbilityTriggersBeforeSacrifice() {
        harness.setHand(player1, List.of(new Flash(), new VenerableMonk()));
        castFlash();

        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertNotOnBattlefield(player1, "Venerable Monk");
        harness.assertInGraveyard(player1, "Venerable Monk");
    }

    @Test
    @DisplayName("A creature with an X cost can be kept by paying its colored cost")
    void xCostCreatureUsesZeroForX() {
        harness.setHand(player1, List.of(new Flash(), new LightningSerpent()));
        castFlash();

        harness.handleCardChosen(player1, 0);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Lightning Serpent");
        Permanent serpent = findPermanent(player1, "Lightning Serpent");
        assertThat(serpent.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO)).isZero();
        harness.assertNotInGraveyard(player1, "Lightning Serpent");
    }

    @Test
    @DisplayName("A Phyrexian mana component can be paid with life")
    void phyrexianManaCanBePaidWithLife() {
        harness.setHand(player1, List.of(new Flash(), new PorcelainLegionnaire()));
        castFlash();

        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Porcelain Legionnaire");
        harness.assertLife(player1, 18);
    }
}
