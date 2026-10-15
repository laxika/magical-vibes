package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BloodMoon;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoldeviExcavations.class, Island.class, BloodMoon.class})
class SoldeviExcavationsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering sacrifices a chosen untapped Island and the land enters")
    void entersBySacrificingUntappedIsland() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new SoldeviExcavations()));

        harness.playLand(player1, 0);

        harness.handlePermanentChosen(player1, island.getId());

        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInGraveyard(player1, "Island");
        harness.assertOnBattlefield(player1, "Soldevi Excavations");
    }

    @Test
    @DisplayName("Declining the sacrifice puts the land into its owner's graveyard")
    void declinedSacrificeSendsLandToGraveyard() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new SoldeviExcavations()));

        harness.playLand(player1, 0);

        harness.handlePermanentChosen(player1, player1.getId());

        harness.assertOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Soldevi Excavations");
        harness.assertInGraveyard(player1, "Soldevi Excavations");
    }

    @Test
    @DisplayName("With no untapped Island the land goes straight to the graveyard without a prompt")
    void noUntappedIslandSendsLandToGraveyard() {
        harness.addToBattlefieldAndReturn(player1, new Island()).tap();
        harness.setHand(player1, List.of(new SoldeviExcavations()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Soldevi Excavations");
        harness.assertInGraveyard(player1, "Soldevi Excavations");
    }

    @Test
    @DisplayName("Mana ability adds {C} and {U}")
    void manaAbilityAddsColorlessAndBlue() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SoldeviExcavations());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Scry ability enters the scry interaction with one card")
    void scryAbilityEntersScryState() {
        harness.addToBattlefield(player1, new SoldeviExcavations());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    void opponentIslandCannotPayEntryReplacement() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new SoldeviExcavations()));
        harness.playLand(player1, 0);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void scryAbilityCanPutCardOnBottom() {
        harness.addToBattlefield(player1, new SoldeviExcavations());
        Card topCard = new Island();
        Card remainingCard = new Island();
        harness.setLibrary(player1, List.of(topCard, remainingCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard, topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void entrySacrificesOnlyTheChosenUntappedIsland() {
        Permanent tappedIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        tappedIsland.tap();
        Permanent chosenIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent otherIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new SoldeviExcavations()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, chosenIsland.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tappedIsland, otherIsland)
                .doesNotContain(chosenIsland).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(chosenIsland.getCard());
        harness.assertOnBattlefield(player1, "Soldevi Excavations");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void scryAbilityCanKeepCardOnTopAndPaysItsCosts() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SoldeviExcavations());
        Card topCard = new Island();
        Card remainingCard = new Island();
        harness.setLibrary(player1, List.of(topCard, remainingCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, remainingCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void scryWithEmptyLibraryResolvesWithoutAChoice() {
        harness.addToBattlefield(player1, new SoldeviExcavations());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void bloodMoonAllowsEntryWithoutAnIsland() {
        harness.addToBattlefield(player2, new BloodMoon());
        harness.setHand(player1, List.of(new SoldeviExcavations()));

        harness.playLand(player1, 0);

        harness.assertOnBattlefield(player1, "Soldevi Excavations");
        harness.assertNotInGraveyard(player1, "Soldevi Excavations");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void bloodMoonDoesNotRequireSacrificingAnAvailableIsland() {
        harness.addToBattlefield(player2, new BloodMoon());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new SoldeviExcavations()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Soldevi Excavations");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(island).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
