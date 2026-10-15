package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.b.BrindleBoar;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SliverConstruct;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LifebaneZombie.class, ChildOfNight.class, BrindleBoar.class, Divination.class,
        SerraAngel.class, Shock.class, GiantGrowth.class, SliverConstruct.class})
class LifebaneZombieTest extends BaseCardTest {

    private void castAndResolveETB() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LifebaneZombie()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0, 0, player2.getId());
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Choosing a green creature card exiles it permanently")
    void choosingGreenCreatureExilesIt() {
        harness.setHand(player2, new ArrayList<>(List.of(new BrindleBoar(), new Divination())));

        castAndResolveETB();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).exileMode()).isTrue();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Brindle Boar"));
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Divination");
    }

    @Test
    @DisplayName("Only green and white creature cards are valid choices")
    void onlyGreenAndWhiteCreaturesAreValid() {
        Card green = new BrindleBoar();
        Card black = new ChildOfNight();
        Card white = new SerraAngel();
        Card sorcery = new Divination();
        harness.setHand(player2, new ArrayList<>(List.of(green, black, white, sorcery)));

        castAndResolveETB();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0, 2);
    }

    @Test
    @DisplayName("Hand without green or white creatures gives no choices")
    void noValidChoicesWhenNoGreenOrWhiteCreatures() {
        harness.setHand(player2, new ArrayList<>(List.of(new ChildOfNight(), new Divination())));

        castAndResolveETB();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Exiled card does not return when the Zombie dies")
    void exiledCardStaysExiledWhenZombieDies() {
        harness.setHand(player2, new ArrayList<>(List.of(new SerraAngel())));

        castAndResolveETB();
        harness.handleCardChosen(player1, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID zombieId = harness.getPermanentId(player1, "Lifebane Zombie");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, zombieId);

        harness.assertNotOnBattlefield(player1, "Lifebane Zombie");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Serra Angel"));
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void emptyHandCompletesWithoutAChoice() {
        harness.setHand(player2, List.of());

        castAndResolveETB();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void greenNoncreatureCannotBeChosen() {
        Card instant = new GiantGrowth();
        Card creature = new BrindleBoar();
        harness.setHand(player2, List.of(instant, creature));

        castAndResolveETB();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(instant);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
    }

    @Test
    void controllerMustChooseExactlyOneCreature() {
        Card green = new BrindleBoar();
        Card white = new SerraAngel();
        harness.setHand(player2, List.of(green, white));

        castAndResolveETB();

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(green);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(white);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void intimidateRejectsGreenNonartifactBlocker() {
        Permanent zombie = addCreatureReady(player1, new LifebaneZombie());
        addCreatureReady(player2, new BrindleBoar());
        zombie.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("intimidate");
    }

    @Test
    void intimidateAllowsBlackBlocker() {
        Permanent zombie = addCreatureReady(player1, new LifebaneZombie());
        Permanent blocker = addCreatureReady(player2, new ChildOfNight());
        zombie.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void intimidateAllowsColorlessArtifactBlocker() {
        Permanent zombie = addCreatureReady(player1, new LifebaneZombie());
        Permanent blocker = addCreatureReady(player2, new SliverConstruct());
        zombie.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
