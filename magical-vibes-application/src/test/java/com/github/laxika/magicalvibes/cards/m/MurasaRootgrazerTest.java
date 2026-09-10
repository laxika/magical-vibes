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
}
