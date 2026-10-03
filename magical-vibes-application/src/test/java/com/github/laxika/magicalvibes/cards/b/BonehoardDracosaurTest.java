package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinTombRaider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BonehoardDracosaur.class, Forest.class, GoblinTombRaider.class})
class BonehoardDracosaurTest extends BaseCardTest {

    @Test
    @DisplayName("Two exiled lands create one Dinosaur token")
    void twoLandsCreateOneDinosaur() {
        List<Card> exiled = resolveWithLibrary(new Forest(), new Forest());

        assertThat(findPermanents(player1, "Dinosaur")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(exiled).hasSize(2);
        assertThat(exiled).allSatisfy(card -> assertThat(gd.exilePlayPermissions)
                .containsEntry(card.getId(), player1.getId()));

        Permanent dinosaur = findPermanent(player1, "Dinosaur");
        assertThat(dinosaur.getCard().getSubtypes()).contains(CardSubtype.DINOSAUR);
        assertThat(gqs.getEffectivePower(gd, dinosaur)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dinosaur)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two exiled nonlands create one Treasure token")
    void twoNonlandsCreateOneTreasure() {
        List<Card> exiled = resolveWithLibrary(new GoblinTombRaider(), new GoblinTombRaider());

        assertThat(findPermanents(player1, "Dinosaur")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(exiled).hasSize(2);
    }

    @Test
    @DisplayName("A land and a nonland create one Dinosaur and one Treasure")
    void mixedCardsCreateBothTokens() {
        resolveWithLibrary(new Forest(), new GoblinTombRaider());

        assertThat(findPermanents(player1, "Dinosaur")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void emptyLibraryCreatesNoTokens() {
        assertThat(resolveWithLibrary()).isEmpty();
        assertThat(findPermanents(player1, "Dinosaur")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void singleLandCreatesOnlyDinosaur() {
        Forest land = new Forest();
        assertThat(resolveWithLibrary(land)).containsExactly(land);
        assertThat(findPermanents(player1, "Dinosaur")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void singleNonlandCreatesOnlyTreasure() {
        GoblinTombRaider creature = new GoblinTombRaider();
        assertThat(resolveWithLibrary(creature)).containsExactly(creature);
        assertThat(findPermanents(player1, "Dinosaur")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void onlyTopTwoCardsAreExiled() {
        Forest first = new Forest();
        GoblinTombRaider second = new GoblinTombRaider();
        Forest third = new Forest();
        assertThat(resolveWithLibrary(first, second, third)).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    void opponentsUpkeepDoesNotTrigger() {
        harness.addToBattlefield(player1, new BonehoardDracosaur());
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void exiledCreatureRequiresNormalTimingAndMana() {
        GoblinTombRaider creature = new GoblinTombRaider();
        resolveWithLibrary(creature);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.playerManaPools.get(player1.getId()).clear();
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goblin Tomb Raider");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void exiledLandsRespectTimingAndLandLimit() {
        Forest first = new Forest();
        Forest second = new Forest();
        resolveWithLibrary(first, second);
        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.castFromExile(player1, first.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, first.getId());
        harness.assertOnBattlefield(player1, "Forest");
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
    }

    @Test
    void permissionExpiresButUnplayedCardsRemainExiled() {
        Forest land = new Forest();
        GoblinTombRaider creature = new GoblinTombRaider();
        resolveWithLibrary(land, creature);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land, creature);
        assertThat(gd.exilePlayPermissions).doesNotContainKeys(land.getId(), creature.getId());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<Card> resolveWithLibrary(Card... cards) {
        harness.addToBattlefield(player1, new BonehoardDracosaur());
        harness.setLibrary(player1, List.of(cards));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        return gd.getPlayerExiledCards(player1.getId());
    }
}
