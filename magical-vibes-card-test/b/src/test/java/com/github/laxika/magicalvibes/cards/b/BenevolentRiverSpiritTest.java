package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.t.TurtleDuck;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BenevolentRiverSpirit.class, TurtleDuck.class, BendersWaterskin.class, BoomerangBasics.class})
class BenevolentRiverSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Waterbend taps five creatures while casting")
    void waterbendTapsFiveCreatures() {
        List<Permanent> creatures = addCreatures(5);
        prepareCast(ManaColor.BLUE, 2);

        harness.castCreatureTappingPermanents(player1, 0, idsOf(creatures));

        assertThat(creatures).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Available mana reduces the number of permanents tapped for waterbend")
    void availableManaReducesWaterbendTaps() {
        List<Permanent> creatures = addCreatures(4);
        prepareCast(ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureTappingPermanents(player1, 0, idsOf(creatures));

        assertThat(creatures).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Entering the battlefield scries two cards")
    void entersAndScriesTwo() {
        List<Permanent> creatures = addCreatures(5);
        harness.setLibrary(player1, List.of(new TurtleDuck(), new TurtleDuck()));
        prepareCast(ManaColor.BLUE, 2);

        harness.castCreatureTappingPermanents(player1, 0, idsOf(creatures));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Benevolent River Spirit");

        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void waterbendCanBePaidEntirelyWithMana() {
        List<Permanent> creatures = addCreatures(5);
        prepareCast(ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Benevolent River Spirit");
        assertThat(creatures).noneMatch(Permanent::isTapped);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void waterbendCanTapArtifactsAndSummoningSickCreatures() {
        List<Permanent> permanents = addCreatures(4);
        permanents.add(harness.addToBattlefieldAndReturn(player1, new BendersWaterskin()));
        prepareCast(ManaColor.BLUE, 2);

        harness.castCreatureTappingPermanents(player1, 0, idsOf(permanents));
        harness.passBothPriorities();

        assertThat(permanents).allMatch(Permanent::isTapped);
        harness.assertOnBattlefield(player1, "Benevolent River Spirit");
    }

    @Test
    void waterbendCannotReplaceBlueMana() {
        List<Permanent> creatures = addCreatures(5);
        prepareCast(ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(player1, 0, idsOf(creatures)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Benevolent River Spirit");
        assertThat(creatures).noneMatch(Permanent::isTapped);
    }

    @Test
    void cannotCastWithoutPayingAdditionalCost() {
        prepareCast(ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Benevolent River Spirit");
    }

    @Test
    void wardCountersOpponentSpellWithoutPayment() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new BenevolentRiverSpirit());
        harness.setHand(player2, List.of(new BoomerangBasics()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, spirit.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Benevolent River Spirit");
        harness.assertInGraveyard(player2, "Boomerang Basics");
    }

    @Test
    void payingWardAllowsOpponentSpellToResolve() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new BenevolentRiverSpirit());
        harness.setHand(player2, List.of(new BoomerangBasics()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, spirit.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Benevolent River Spirit");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void controllerCanTargetSpiritWithoutPayingWard() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new BenevolentRiverSpirit());
        harness.setHand(player1, List.of(new BoomerangBasics()));
        harness.setLibrary(player1, List.of(new TurtleDuck()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, spirit.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Benevolent River Spirit");
        harness.assertInHand(player1, "Turtle-Duck");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void enteringWithoutCastingStillScriesAndCanBottomCards() {
        TurtleDuck first = new TurtleDuck();
        BendersWaterskin second = new BendersWaterskin();
        BenevolentRiverSpirit third = new BenevolentRiverSpirit();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.enterBattlefieldAndReturn(player1, new BenevolentRiverSpirit());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void enteringWithOneCardInLibraryScriesThatCard() {
        TurtleDuck card = new TurtleDuck();
        harness.setLibrary(player1, List.of(card));

        harness.enterBattlefieldAndReturn(player1, new BenevolentRiverSpirit());
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(card);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
    }

    @Test
    void enteringWithEmptyLibraryCompletesWithoutInteraction() {
        harness.setLibrary(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new BenevolentRiverSpirit());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Benevolent River Spirit");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private List<Permanent> addCreatures(int count) {
        List<Permanent> creatures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            creatures.add(harness.addToBattlefieldAndReturn(player1, new TurtleDuck()));
        }
        return creatures;
    }

    private void prepareCast(ManaColor color, int amount) {
        harness.setHand(player1, List.of(new BenevolentRiverSpirit()));
        harness.addMana(player1, color, amount);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    private List<UUID> idsOf(List<Permanent> permanents) {
        return permanents.stream().map(Permanent::getId).toList();
    }
}
