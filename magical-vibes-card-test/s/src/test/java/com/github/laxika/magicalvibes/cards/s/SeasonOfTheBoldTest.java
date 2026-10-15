package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeasonOfTheBold.class, GrizzlyBears.class, Plains.class, Shock.class})
class SeasonOfTheBoldTest extends BaseCardTest {

    @Test
    @DisplayName("Creates tapped Treasures and allows repeating a mode")
    void createsTappedTreasuresWithRepeatedMode() {
        cast(mode(0, 0));

        List<Permanent> treasures = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(treasures).hasSize(2).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Exiles the top two cards with play permission through the next turn")
    void exilesTopTwoCards() {
        Card first = new Plains();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));

        cast(mode(1));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.exilePlayPermissions).containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
    }

    @Test
    @DisplayName("The spell-cast mode deals damage to an optionally chosen creature")
    void spellCastModeDamagesChosenCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(mode(2));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The spell-cast mode can resolve without a creature target")
    void spellCastModeCanDeclineTarget() {
        cast(mode(2));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void canChooseNoModes() {
        Card topCard = new Plains();
        harness.setLibrary(player1, List.of(topCard));

        cast(mode());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertInGraveyard(player1, "Season of the Bold");
    }

    @Test
    void createsFiveTappedTreasures() {
        cast(mode(0, 0, 0, 0, 0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(5)
                .allSatisfy(permanent -> {
                    assertThat(permanent.getCard().getName()).isEqualTo("Treasure");
                    assertThat(permanent.isTapped()).isTrue();
                });
    }

    @Test
    void repeatedExileModeCombinesWithTreasureMode() {
        Card first = new Plains();
        Card second = new GrizzlyBears();
        Card third = new Shock();
        Card fourth = new Plains();
        Card fifth = new Plains();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth));

        cast(mode(1, 0, 1));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second, third, fourth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .allMatch(Permanent::isTapped);
        for (Card card : List.of(first, second, third, fourth)) {
            assertThat(gd.exilePlayPermissions).containsEntry(card.getId(), player1.getId());
        }
    }

    @Test
    void exileModeHandlesShortAndEmptyLibraries() {
        Card onlyCard = new Plains();
        harness.setLibrary(player1, List.of(onlyCard));

        cast(mode(1, 1));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).containsEntry(onlyCard.getId(), player1.getId());
    }

    @Test
    void exiledLandAndCreatureCanBePlayed() {
        Card land = new Plains();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, creature));
        cast(mode(1));

        harness.castFromExile(player1, land.getId());
        harness.assertOnBattlefield(player1, "Plains");
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void mixedExileAndTriggerModesDamageBeforeExiledSpellResolves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock, new Plains()));
        cast(mode(1, 2));

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, shock.getId(), player2.getId());
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    void canDeclineDamageEvenWhenCreatureIsAvailable() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(mode(2));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(bear.getMarkedDamage()).isZero();
        harness.assertLife(player2, 18);
    }

    @Test
    void opponentsSpellsDoNotTriggerDamage() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        cast(mode(2));

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void permissionsAndDamageTriggerLastThroughNextTurnAndThenExpire() {
        Card exiledShock = new Shock();
        Card land = new Plains();
        harness.setLibrary(player1, List.of(exiledShock, land,
                new Plains(), new Plains(), new Plains(), new Plains()));
        harness.setLibrary(player2, List.of(new Plains(), new Plains(), new Plains(), new Plains()));
        cast(mode(1, 2));

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        Permanent firstBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, firstBear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, exiledShock.getId(), player2.getId());
        harness.handlePermanentChosen(player1, secondBear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.exilePlayPermissions).containsEntry(land.getId(), player1.getId());

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(land.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(land);
    }

    private void cast(int selection) {
        harness.setHand(player1, List.of(new SeasonOfTheBold()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, selection);
    }

    private static int mode(int... modeIndices) {
        return ChooseOneEffect.encodeBudgetedModeSelection(5, List.of(1, 2, 3), modeIndices);
    }
}
