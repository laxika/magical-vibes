package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KarnScionOfUrza;
import com.github.laxika.magicalvibes.cards.m.MoxAmber;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.ShizoDeathsStorehouse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KethisTheHiddenHand.class, Forest.class, GrizzlyBears.class, KarnScionOfUrza.class,
        MoxAmber.class, Murder.class, ShizoDeathsStorehouse.class})
class KethisTheHiddenHandTest extends BaseCardTest {

    @Test
    @DisplayName("Legendary spells cost one less to cast")
    void legendarySpellsCostOneLess() {
        harness.addToBattlefield(player1, new KethisTheHiddenHand());
        harness.setHand(player1, List.of(new KarnScionOfUrza()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        harness.castPlaneswalker(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.PLANESWALKER_SPELL);
    }

    @Test
    @DisplayName("Nonlegendary spells do not receive the cost reduction")
    void nonlegendarySpellsAreNotReduced() {
        harness.addToBattlefield(player1, new KethisTheHiddenHand());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activation exiles only legendary cards and grants play permission to remaining legends")
    void activatesForLegendaryCards() {
        MoxAmber first = new MoxAmber();
        ShizoDeathsStorehouse second = new ShizoDeathsStorehouse();
        MoxAmber playable = new MoxAmber();
        Forest forest = new Forest();
        harness.addToBattlefield(player1, new KethisTheHiddenHand());
        harness.setGraveyard(player1, List.of(forest, first, second, playable));
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, null);
        PendingInteraction.ActivatedAbilityGraveyardExileCostChoice choice =
                gd.interaction.activeInteraction(
                        PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class);
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId(), playable.getId());
        harness.handleMultipleCardsChosen(player1, List.of(second.getId(), playable.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(second, playable);
        harness.assertInGraveyard(player1, "Forest");

        harness.castFromGraveyard(player1, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mox Amber");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activation permits a legendary land to be played from the graveyard")
    void playsLegendaryLandFromGraveyard() {
        harness.addToBattlefield(player1, new KethisTheHiddenHand());
        harness.setGraveyard(player1, List.of(
                new MoxAmber(), new ShizoDeathsStorehouse(), new ShizoDeathsStorehouse()));
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(
                gd.playerGraveyards.get(player1.getId()).get(0).getId(),
                gd.playerGraveyards.get(player1.getId()).get(2).getId()));
        harness.passBothPriorities();

        harness.playGraveyardLand(player1, 0);

        harness.assertOnBattlefield(player1, "Shizo, Death's Storehouse");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The graveyard play permission expires at end of turn")
    void graveyardPlayPermissionExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new KethisTheHiddenHand());
        harness.setGraveyard(player1, List.of(new MoxAmber(), new ShizoDeathsStorehouse(), new MoxAmber()));
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, gd.playerGraveyards.get(player1.getId()).stream()
                .limit(2)
                .map(Card::getId)
                .toList());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        prepareMainPhase();
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsLegendarySpellsAreNotReduced() {
        harness.addToBattlefield(player2, new KethisTheHiddenHand());
        harness.setHand(player1, List.of(new KarnScionOfUrza()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castPlaneswalker(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void reductionDoesNotPayColoredMana() {
        harness.addToBattlefield(player1, new KethisTheHiddenHand());
        harness.setHand(player1, List.of(new KethisTheHiddenHand()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void legendarySpellsCastFromGraveyardReceiveCostReduction() {
        MoxAmber first = new MoxAmber();
        MoxAmber second = new MoxAmber();
        KarnScionOfUrza karn = new KarnScionOfUrza();
        harness.addToBattlefield(player1, new KethisTheHiddenHand());
        harness.setGraveyard(player1, List.of(first, second, karn));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Karn, Scion of Urza");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotActivateWithOnlyOneLegendaryCardInGraveyard() {
        MoxAmber legend = new MoxAmber();
        Forest forest = new Forest();
        harness.addToBattlefield(player1, new KethisTheHiddenHand());
        harness.setGraveyard(player1, List.of(legend, forest));
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(legend, forest);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activation rejects nonlegendary cards as payment")
    void rejectsNonlegendaryCardsAsPayment() {
        Forest forest = new Forest();
        MoxAmber first = new MoxAmber();
        MoxAmber second = new MoxAmber();
        MoxAmber third = new MoxAmber();
        harness.addToBattlefield(player1, new KethisTheHiddenHand());
        harness.setGraveyard(player1, List.of(forest, first, second, third));
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(forest.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(forest, first, second, third);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void permissionSurvivesSourceLeavingButDoesNotIncludeLaterGraveyardArrivals() {
        KethisTheHiddenHand kethis = new KethisTheHiddenHand();
        Murder murder = new Murder();
        MoxAmber first = new MoxAmber();
        MoxAmber second = new MoxAmber();
        MoxAmber playable = new MoxAmber();
        harness.addToBattlefield(player1, kethis);
        harness.setGraveyard(player1, List.of(first, second, playable));
        harness.setHand(player1, List.of(murder));
        harness.addMana(player1, ManaColor.BLACK, 3);
        prepareMainPhase();

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Kethis, the Hidden Hand"));

        harness.assertInGraveyard(player1, "Kethis, the Hidden Hand");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 1))
                .isInstanceOf(IllegalStateException.class);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Mox Amber");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(kethis, murder);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
