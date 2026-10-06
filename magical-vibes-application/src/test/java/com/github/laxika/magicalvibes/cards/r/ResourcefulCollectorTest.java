package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ResourcefulCollector.class, Forest.class, GrizzlyBears.class, Recover.class, Shock.class})
class ResourcefulCollectorTest extends BaseCardTest {

    @Test
    void makesAnEligibleGraveyardPermanentFoodAndPlayable() {
        harness.addToBattlefield(player1, new ResourcefulCollector());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears, new Shock()));

        advanceToEndStep();

        Card foodCard = gd.playerGraveyards.get(player1.getId()).getFirst();
        assertThat(foodCard.getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(foodCard.getSubtypes()).contains(CardSubtype.FOOD);
        assertThat(gd.graveyardPlayPermissions).containsEntry(foodCard.getId(), player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent food = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.isArtifact(gd, food)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, food, CardSubtype.FOOD)).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food), 0, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void doesNotConvertCardsDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new ResourcefulCollector());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.graveyardPlayPermissions).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst().getSubtypes())
                .doesNotContain(CardSubtype.FOOD);
        assertThat(gd.playerGraveyards.get(player2.getId()).getFirst().getSubtypes())
                .doesNotContain(CardSubtype.FOOD);
    }

    @Test
    void doesNothingWhenOnlyNonPermanentCardsAreInControllersGraveyard() {
        harness.addToBattlefield(player1, new ResourcefulCollector());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        advanceToEndStep();

        assertThat(gd.graveyardPlayPermissions).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst().getSubtypes())
                .doesNotContain(CardSubtype.FOOD);
        assertThat(gd.playerGraveyards.get(player2.getId()).getFirst().getSubtypes())
                .doesNotContain(CardSubtype.FOOD);
    }

    @Test
    void separateCollectorsConvertDifferentNonFoodCards() {
        harness.addToBattlefield(player1, new ResourcefulCollector());
        harness.addToBattlefield(player1, new ResourcefulCollector());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Shock()));

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getSubtypes().contains(CardSubtype.FOOD))
                .hasSize(2);
        assertThat(gd.graveyardPlayPermissions).hasSize(2);
    }

    @Test
    void convertedLandCanBePlayedAndExiledForLifeWithoutTapping() {
        harness.addToBattlefield(player1, new ResourcefulCollector());
        harness.setGraveyard(player1, List.of(new Forest()));
        advanceToEndStep();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLandFromGraveyard(player1, 0);

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(gqs.isArtifact(gd, forest)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, forest, CardSubtype.FOOD)).isTrue();
        assertThat(gd.graveyardPlayPermissions).isEmpty();
        forest.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(forest),
                0, null, null);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertLife(player1, 20);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
    }

    @Test
    void permissionDoesNotOverrideCreatureTiming() {
        harness.addToBattlefield(player1, new ResourcefulCollector());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        advanceToEndStep();
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void perpetualFoodSurvivesDeathButPlayPermissionDoesNot() {
        harness.addToBattlefield(player1, new ResourcefulCollector());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        advanceToEndStep();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .allSatisfy(card -> {
                    assertThat(card.getSubtypes()).contains(CardSubtype.FOOD);
                    assertThat(card.getAdditionalTypes()).contains(CardType.ARTIFACT);
                });
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int bearsIndex = gd.playerGraveyards.get(player1.getId()).indexOf(bears.getCard());
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, bearsIndex))
                .isInstanceOf(IllegalStateException.class);

        advanceToEndStep();

        assertThat(gd.graveyardPlayPermissions).isEmpty();
    }

    @Test
    void returningConvertedCardToHandEndsItsGraveyardPlayPermission() {
        harness.addToBattlefield(player1, new ResourcefulCollector());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        advanceToEndStep();
        Card converted = gd.playerGraveyards.get(player1.getId()).getFirst();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Recover()));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0, converted.getId());
        harness.assertInHand(player1, "Grizzly Bears");

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, gd.playerHands.get(player1.getId()).indexOf(converted));
        harness.passBothPriorities();
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.RED, 1);
        int shockIndex = gd.playerHands.get(player1.getId()).stream()
                .map(Card::getName).toList().indexOf("Shock");
        harness.castAndResolveInstant(player1, shockIndex, bears.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, converted.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void conversionAndPlayPermissionPersistUntilNextTurn() {
        harness.addToBattlefield(player1, new ResourcefulCollector());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));
        harness.setLibrary(player2, List.of(new Shock(), new Shock()));
        advanceToEndStep();

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.isArtifact(gd, bears)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.FOOD)).isTrue();
    }

    @Test
    void emptyGraveyardDoesNotRequireAChoice() {
        harness.addToBattlefield(player1, new ResourcefulCollector());
        harness.setGraveyard(player1, List.of());

        advanceToEndStep();

        assertThat(gd.graveyardPlayPermissions).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
