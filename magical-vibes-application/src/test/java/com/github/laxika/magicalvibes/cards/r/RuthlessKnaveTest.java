package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.q.QueensBaySoldier;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RuthlessKnave.class, QueensBaySoldier.class, RaptorCompanion.class})
class RuthlessKnaveTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a chosen creature and creates two Treasures")
    void sacrificesChosenCreatureAndCreatesTreasures() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, new QueensBaySoldier());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Queen's Bay Soldier").getId());

        harness.assertNotOnBattlefield(player1, "Queen's Bay Soldier");
        harness.assertInGraveyard(player1, "Queen's Bay Soldier");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities();

        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(2);
        for (Permanent treasure : treasures) {
            assertThat(treasure.getCard().getType()).isEqualTo(CardType.ARTIFACT);
            assertThat(treasure.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
        }
    }

    @Test
    @DisplayName("Prompts for creature choice when multiple other creatures available")
    void promptsForCreatureChoiceWhenMultipleAvailable() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, new QueensBaySoldier());
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Choosing a creature to sacrifice puts ability on stack")
    void choosingCreaturePutsAbilityOnStack() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new QueensBaySoldier());
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, soldier.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Queen's Bay Soldier");
        harness.assertOnBattlefield(player1, "Raptor Companion");
    }

    @Test
    @DisplayName("Mana is consumed when activating treasure ability")
    void manaIsConsumedForTreasureAbility() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, new QueensBaySoldier());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate treasure ability without enough mana")
    void cannotActivateTreasureAbilityWithoutEnoughMana() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, new QueensBaySoldier());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Can sacrifice Knave itself when it is the only creature")
    void canSacrificeKnaveWhenItIsOnlyCreature() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Ruthless Knave");
        harness.assertInGraveyard(player1, "Ruthless Knave");
        assertThat(gd.stack).hasSize(1);
        assertThat(countPermanents(player1, "Treasure")).isZero();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Test
    @DisplayName("Can activate treasure ability multiple times with enough resources")
    void canActivateTreasureAbilityMultipleTimes() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, new QueensBaySoldier());
        harness.addToBattlefield(player1, new RaptorCompanion());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID soldierId = findPermanent(player1, "Queen's Bay Soldier").getId();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, soldierId);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Raptor Companion").getId());
        harness.passBothPriorities();

        long treasureCount = countPermanents(player1, "Treasure");
        assertThat(treasureCount).isEqualTo(4);
    }

    @Test
    @DisplayName("Auto-sacrifices when exactly 3 Treasures available")
    void autoSacrificesWhenExactlyThreeTreasures() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(countPermanents(player1, "Treasure")).isZero();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving draw ability draws a card")
    void resolvingDrawAbilityDrawsCard() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());

        int startingHandSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(startingHandSize + 1);
    }

    @Test
    @DisplayName("Prompts for Treasure choice when more than 3 available")
    void promptsForChoiceWhenMoreThanThreeTreasures() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Completing three sacrifice choices puts ability on stack")
    void completingThreeSacrificesPutsAbilityOnStack() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        UUID t1Id = harness.addToBattlefieldAndReturn(player1, createTreasureToken()).getId();
        UUID t2Id = harness.addToBattlefieldAndReturn(player1, createTreasureToken()).getId();
        UUID t3Id = harness.addToBattlefieldAndReturn(player1, createTreasureToken()).getId();
        harness.addToBattlefield(player1, createTreasureToken());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, t1Id);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, t2Id);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, t3Id);

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.ACTIVATED_ABILITY);

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate draw ability without 3 Treasures")
    void cannotActivateDrawAbilityWithoutThreeTreasures() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Cannot activate draw ability with no Treasures")
    void cannotActivateDrawAbilityWithNoTreasures() {
        harness.addToBattlefield(player1, new RuthlessKnave());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Draw ability does not require mana")
    void drawAbilityDoesNotRequireMana() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can sacrifice a creature to create Treasures then sacrifice Treasures to draw")
    void canCreateTreasuresThenSacrificeThemToDraw() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, new QueensBaySoldier());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Queen's Bay Soldier").getId());
        harness.passBothPriorities();

        long treasureCount = countPermanents(player1, "Treasure");
        assertThat(treasureCount).isEqualTo(3);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Can choose Knave itself even when another creature is available")
    void canChooseKnaveWithAnotherCreatureAvailable() {
        Permanent knave = harness.addToBattlefieldAndReturn(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, new QueensBaySoldier());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, knave.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ruthless Knave");
        harness.assertOnBattlefield(player1, "Queen's Bay Soldier");
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's Treasures cannot pay the draw ability's cost")
    void cannotSacrificeOpponentsTreasures() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player1, createTreasureToken());
        harness.addToBattlefield(player2, createTreasureToken());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
        assertThat(countPermanents(player2, "Treasure")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapped Treasures can be sacrificed to draw")
    void canSacrificeTappedTreasures() {
        harness.addToBattlefield(player1, new RuthlessKnave());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefieldAndReturn(player1, createTreasureToken()).setTapped(true);
        }
        harness.setLibrary(player1, List.of(new QueensBaySoldier()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertInHand(player1, "Queen's Bay Soldier");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private Card createTreasureToken() {
        Card card = new Card();
        card.setName("Treasure");
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{0}");
        card.setSubtypes(List.of(CardSubtype.TREASURE));
        return card;
    }

}
