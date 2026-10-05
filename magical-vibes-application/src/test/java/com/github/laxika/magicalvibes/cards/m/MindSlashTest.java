package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DisruptingScepter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({MindSlash.class, GrizzlyBears.class, DisruptingScepter.class, Swamp.class})
class MindSlashTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature reveals opponent's hand and discards the chosen card")
    void sacrificeRevealsHandAndDiscardsChosenCard() {
        int index = setupMindSlashWithCreature(player1);
        harness.setHand(player2, List.of(new GrizzlyBears(), new DisruptingScepter()));

        harness.activateAbility(player1, index, null, player2.getId());

        // The lone creature is auto-sacrificed as the cost.
        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).choosingPlayerId())
                .isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Disrupting Scepter");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate at sorcery speed during opponent's turn")
    void cannotActivateDuringOpponentsTurn() {
        int index = addMindSlash(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void cannotActivateWithoutCreature() {
        int index = addMindSlash(player1);
        prepareSorcerySpeedActivation(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target self")
    void cannotTargetSelf() {
        int index = setupMindSlashWithCreature(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without the black mana in its cost")
    void cannotActivateWithoutBlackMana() {
        int index = addMindSlash(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        prepareSorcerySpeedActivation(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot sacrifice a noncreature permanent as the activation cost")
    void cannotSacrificeNoncreaturePermanent() {
        int index = addMindSlash(player1);
        harness.addToBattlefield(player1, new DisruptingScepter());
        prepareSorcerySpeedActivation(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotInGraveyard(player1, "Disrupting Scepter");
    }

    @Test
    @DisplayName("Resolves without a discard when the opponent's hand is empty")
    void resolvesWithEmptyOpponentHand() {
        int index = setupMindSlashWithCreature(player1);
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, index, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A land can be chosen for discard")
    void canDiscardLand() {
        int index = setupMindSlashWithCreature(player1);
        harness.setHand(player2, List.of(new Swamp(), new GrizzlyBears()));

        harness.activateAbility(player1, index, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Swamp");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature can be chosen for discard")
    void canDiscardCreature() {
        int index = setupMindSlashWithCreature(player1);
        harness.setHand(player2, List.of(new GrizzlyBears(), new Swamp()));

        harness.activateAbility(player1, index, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Swamp");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate outside a main phase")
    void cannotActivateDuringUpkeep() {
        int index = setupMindSlashWithCreature(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while another ability is on the stack")
    void cannotActivateWithNonemptyStack() {
        int index = setupMindSlashWithCreature(player1);
        harness.setHand(player2, List.of(new Swamp()));
        harness.activateAbility(player1, index, null, player2.getId());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        int index = addMindSlash(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        prepareSorcerySpeedActivation(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing among creatures sacrifices exactly one before resolution")
    void choosesOneCreatureToSacrifice() {
        int index = setupMindSlashWithCreature(player1);
        var chosen = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Swamp()));

        harness.activateAbility(player1, index, null, player2.getId());
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Swamp");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Swamp");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private int setupMindSlashWithCreature(Player player) {
        int index = addMindSlash(player);
        harness.addToBattlefield(player, new GrizzlyBears());
        prepareSorcerySpeedActivation(player);
        harness.addMana(player, ManaColor.BLACK, 1);
        return index;
    }

    private int addMindSlash(Player player) {
        harness.addToBattlefield(player, new MindSlash());
        return gd.playerBattlefields.get(player.getId()).size() - 1;
    }

    private void prepareSorcerySpeedActivation(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
