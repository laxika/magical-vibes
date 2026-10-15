package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoralAtoll.class, Island.class, Plains.class})
class CoralAtollTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.addToBattlefield(player1, new Island());
        playAndResolveEtb();

        harness.handleMayAbilityChosen(player1, true);

        Permanent atoll = findPermanent(player1, "Coral Atoll");
        assertThat(atoll.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has no untapped Island")
    void autoSacrificesWithoutUntappedIsland() {
        harness.addToBattlefieldAndReturn(player1, new Island()).tap();
        harness.addToBattlefield(player1, new Plains());
        playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Coral Atoll");
        harness.assertInGraveyard(player1, "Coral Atoll");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("Accepting returns an untapped Island and keeps Coral Atoll")
    void acceptReturnsIslandAndKeepsAtoll() {
        harness.addToBattlefield(player1, new Island());
        playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Coral Atoll");
        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Accepting with two untapped Islands lets controller choose which to return")
    void acceptWithTwoIslandsChoosesOne() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        playAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        UUID islandId = findPermanents(player1, "Island").getFirst().getId();
        harness.handleMultiplePermanentsChosen(player1, List.of(islandId));

        harness.assertOnBattlefield(player1, "Coral Atoll");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island")).count()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Island")).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns a controlled Island to its owner's hand")
    void returnsControlledIslandToOwnersHand() {
        Island island = new Island();
        island.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, island);

        playAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Coral Atoll");
        harness.assertNotOnBattlefield(player1, "Island");
        assertThat(gd.playerHands.get(player2.getId())).contains(island);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(island);
    }

    @Test
    @DisplayName("Declining sacrifices Coral Atoll and keeps the Island")
    void declineSacrificesAtoll() {
        harness.addToBattlefield(player1, new Island());
        playAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Coral Atoll");
        harness.assertInGraveyard(player1, "Coral Atoll");
        harness.assertOnBattlefield(player1, "Island");
    }

    @Test
    @DisplayName("Mana ability adds {C} and {U}")
    void manaAbilityAddsColorlessAndBlue() {
        harness.addToBattlefield(player1, new CoralAtoll());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = findPermanent(player1, "Coral Atoll");
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's untapped Island cannot pay the entry cost")
    void opponentsIslandCannotPayEntryCost() {
        harness.addToBattlefield(player2, new Island());

        playAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Coral Atoll");
        harness.assertInGraveyard(player1, "Coral Atoll");
        harness.assertOnBattlefield(player2, "Island");
        harness.assertNotInHand(player2, "Island");
    }

    @Test
    @DisplayName("Tapping the only Island before the entry trigger resolves prevents returning it")
    void islandTappedInResponseCannotPayEntryCost() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new CoralAtoll()));
        harness.playLand(player1, 0);

        harness.tapPermanent(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Coral Atoll");
        harness.assertInGraveyard(player1, "Coral Atoll");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertNotInHand(player1, "Island");
    }

    @Test
    @DisplayName("Only the untapped Island is returned when another Island is tapped")
    void returnsOnlyEligibleIsland() {
        Permanent tappedIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        tappedIsland.tap();
        Permanent untappedIsland = harness.addToBattlefieldAndReturn(player1, new Island());

        playAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Coral Atoll");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tappedIsland).doesNotContain(untappedIsland);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(untappedIsland.getCard());
    }

    private void playAndResolveEtb() {
        harness.setHand(player1, List.of(new CoralAtoll()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
    }
}
