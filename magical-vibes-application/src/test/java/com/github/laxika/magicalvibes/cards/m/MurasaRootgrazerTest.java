package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AdarkarWastes;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MurasaRootgrazer.class, AdarkarWastes.class, Forest.class, GrizzlyBears.class, Plains.class})
class MurasaRootgrazerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a basic land from hand onto the battlefield untapped")
    void putsBasicLandFromHandOntoBattlefield() {
        Permanent rootgrazer = addReadyRootgrazer(player1);
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(rootgrazer.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only basic lands are offered from hand")
    void onlyOffersBasicLands() {
        addReadyRootgrazer(player1);
        harness.setHand(player1, List.of(new AdarkarWastes(), new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Returns a target basic land you control to its owner's hand")
    void returnsTargetBasicLandYouControl() {
        Permanent rootgrazer = addReadyRootgrazer(player1);
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());

        harness.activateAbility(player1, 0, 1, null, plains.getId());
        harness.passBothPriorities();

        assertThat(rootgrazer.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Plains");
        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("Cannot return a nonbasic land")
    void cannotTargetNonbasicLand() {
        addReadyRootgrazer(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new AdarkarWastes());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot return an opponent's basic land")
    void cannotTargetOpponentsBasicLand() {
        addReadyRootgrazer(player1);
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot return a nonland permanent")
    void cannotTargetNonlandPermanent() {
        addReadyRootgrazer(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyRootgrazer(Player player) {
        return addCreatureReady(player, new MurasaRootgrazer());
    }

    @Test
    @DisplayName("Declining the optional ability leaves the land in hand but pays the tap cost")
    void canDeclinePuttingLandOntoBattlefield() {
        Permanent rootgrazer = addReadyRootgrazer(player1);
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(rootgrazer.isTapped()).isTrue();
        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The first ability resolves without a land when the hand is empty")
    void resolvesWithNoLandInHand() {
        Permanent rootgrazer = addReadyRootgrazer(player1);
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(rootgrazer.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(rootgrazer);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Summoning sickness prevents both tap abilities")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new MurasaRootgrazer());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped Rootgrazer cannot activate either tap ability")
    void cannotActivateWhileTapped() {
        Permanent rootgrazer = addReadyRootgrazer(player1);
        rootgrazer.setTapped(true);
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A basic land controlled by the ability's controller returns to its actual owner")
    void returnsBorrowedLandToOwner() {
        addReadyRootgrazer(player1);
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        gd.playerBattlefields.get(player2.getId()).remove(plains);
        gd.playerBattlefields.get(player1.getId()).add(plains);
        gd.stolenCreatures.put(plains.getId(), player2.getId());

        harness.activateAbility(player1, 0, 1, null, plains.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Plains");
        harness.assertInHand(player2, "Plains");
        harness.assertNotInHand(player1, "Plains");
    }

    @Test
    @DisplayName("The return ability does not return a land that changes controller before resolution")
    void cannotReturnLandNoLongerControlled() {
        addReadyRootgrazer(player1);
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());

        harness.activateAbility(player1, 0, 1, null, plains.getId());
        gd.playerBattlefields.get(player1.getId()).remove(plains);
        gd.playerBattlefields.get(player2.getId()).add(plains);
        gd.stolenCreatures.put(plains.getId(), player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Plains");
        harness.assertNotInHand(player1, "Plains");
        harness.assertNotInHand(player2, "Plains");
        assertThat(gd.stack).isEmpty();
    }
}
