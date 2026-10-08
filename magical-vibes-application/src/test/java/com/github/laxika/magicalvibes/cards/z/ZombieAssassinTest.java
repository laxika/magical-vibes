package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.n.NantukoDisciple;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZombieAssassin.class, Plains.class, NantukoDisciple.class, DuskImp.class})
class ZombieAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles two graveyard cards and itself, and destroys a nonblack creature")
    void destroysNonblackCreature() {
        addReadyAssassin(player1);
        harness.setGraveyard(player1, List.of(new Plains(), new Plains()));
        Permanent target = addCreatureReady(player2, new NantukoDisciple());
        target.setRegenerationShield(1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Zombie Assassin");
        harness.assertNotInGraveyard(player1, "Zombie Assassin");
        harness.assertNotOnBattlefield(player2, "Nantuko Disciple");
        harness.assertInGraveyard(player2, "Nantuko Disciple");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        addReadyAssassin(player1);
        harness.setGraveyard(player1, List.of(new Plains(), new Plains()));
        Permanent target = addCreatureReady(player2, new DuskImp());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot activate without two cards in the graveyard")
    void cannotActivateWithoutTwoGraveyardCards() {
        addReadyAssassin(player1);
        harness.setGraveyard(player1, List.of(new Plains()));
        Permanent target = addCreatureReady(player2, new NantukoDisciple());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReadyAssassin(player1);
        harness.setGraveyard(player1, List.of(new Plains(), new Plains()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Can target a nonblack creature controlled by the activating player")
    void canTargetOwnNonblackCreature() {
        addReadyAssassin(player1);
        harness.setGraveyard(player1, List.of(new Plains(), new Plains()));
        Permanent target = addCreatureReady(player1, new NantukoDisciple());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Zombie Assassin");
        harness.assertNotOnBattlefield(player1, "Nantuko Disciple");
        harness.assertNotInGraveyard(player1, "Zombie Assassin");
        harness.assertInGraveyard(player1, "Nantuko Disciple");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Exiles exactly two chosen cards when more than two are available")
    void exilesExactlyTwoChosenCards() {
        addReadyAssassin(player1);
        Plains first = new Plains();
        Plains second = new Plains();
        Plains remaining = new Plains();
        harness.setGraveyard(player1, List.of(first, second, remaining));
        Permanent target = addCreatureReady(player2, new NantukoDisciple());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first, second).hasSize(3);
        harness.assertNotInGraveyard(player1, "Zombie Assassin");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(remaining)
                .doesNotContain(first, second);
        harness.assertNotOnBattlefield(player2, "Nantuko Disciple");
    }

    @Test
    @DisplayName("Exiles itself and the graveyard cards before the ability resolves")
    void paysExileCostsBeforeResolution() {
        addReadyAssassin(player1);
        ZombieAssassin assassin = (ZombieAssassin) findPermanent(player1, "Zombie Assassin").getCard();
        Plains first = new Plains();
        Plains second = new Plains();
        harness.setGraveyard(player1, List.of(first, second));
        Permanent target = addCreatureReady(player2, new NantukoDisciple());

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Zombie Assassin");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second, assassin);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player2, "Nantuko Disciple");

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Nantuko Disciple");
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        addReadyAssassin(player1);
        findPermanent(player1, "Zombie Assassin").tap();
        harness.setGraveyard(player1, List.of(new Plains(), new Plains()));
        Permanent target = addCreatureReady(player2, new NantukoDisciple());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Zombie Assassin");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        addReadyAssassin(player1);
        findPermanent(player1, "Zombie Assassin").setSummoningSick(true);
        harness.setGraveyard(player1, List.of(new Plains(), new Plains()));
        Permanent target = addCreatureReady(player2, new NantukoDisciple());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Zombie Assassin");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot use cards in the opponent's graveyard to pay the cost")
    void cannotPayWithOpponentsGraveyard() {
        addReadyAssassin(player1);
        harness.setGraveyard(player1, List.of(new Plains()));
        harness.setGraveyard(player2, List.of(new Plains(), new Plains()));
        Permanent target = addCreatureReady(player2, new NantukoDisciple());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Zombie Assassin");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
    private void addReadyAssassin(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addCreatureReady(player, new ZombieAssassin());
    }
}
