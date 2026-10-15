package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.cards.r.RubblebeltRunner;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GateColossus.class, RakdosGuildgate.class, SauroformHybrid.class, RubblebeltRunner.class, Forest.class})
class GateColossusTest extends BaseCardTest {

    @Test
    @DisplayName("Each Gate you control reduces Gate Colossus's generic casting cost by one")
    void gatesReduceCastingCost() {
        harness.addToBattlefield(player1, new RakdosGuildgate());
        harness.addToBattlefield(player1, new RakdosGuildgate());
        harness.addToBattlefield(player2, new RakdosGuildgate());
        harness.setHand(player1, List.of(new GateColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Gate Colossus cannot be blocked by a creature with power two or less")
    void lowPowerCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new GateColossus());
        Permanent blocker = addCreatureReady(player2, new SauroformHybrid());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gate Colossus can be blocked by a creature with power three")
    void higherPowerCreatureCanBlock() {
        Permanent attacker = addCreatureReady(player1, new GateColossus());
        Permanent blocker = addCreatureReady(player2, new RubblebeltRunner());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A Gate entering lets you put Gate Colossus from your graveyard on top of your library")
    void gateEntersMayPutColossusOnTop() {
        GateColossus colossus = new GateColossus();
        Card topCard = new SauroformHybrid();
        harness.setGraveyard(player1, List.of(colossus));
        harness.setLibrary(player1, List.of(topCard));
        prepareMain(player1);

        harness.setHand(player1, List.of(new RakdosGuildgate()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(colossus.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(colossus.getId()));
    }

    @Test
    @DisplayName("Declining the Gate trigger keeps Gate Colossus in the graveyard")
    void gateTriggerCanBeDeclined() {
        GateColossus colossus = new GateColossus();
        harness.setGraveyard(player1, List.of(colossus));
        prepareMain(player1);

        harness.setHand(player1, List.of(new RakdosGuildgate()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(colossus.getId()));
    }

    @Test
    @DisplayName("A non-Gate land does not trigger Gate Colossus")
    void nonGateLandDoesNotTrigger() {
        GateColossus colossus = new GateColossus();
        harness.setGraveyard(player1, List.of(colossus));
        prepareMain(player1);

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("More than eight Gates allow casting without mana")
    void excessGatesReduceCostToZero() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new RakdosGuildgate());
        }
        harness.setHand(player1, List.of(new GateColossus()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An opponent's Gate does not trigger a Colossus in your graveyard")
    void opponentsGateDoesNotTrigger() {
        GateColossus colossus = new GateColossus();
        harness.setGraveyard(player1, List.of(colossus));
        prepareMain(player2);
        harness.setHand(player2, List.of(new RakdosGuildgate()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(colossus);
    }

    @Test
    @DisplayName("A Colossus can return to an empty library without returning other graveyard cards")
    void returnsOnlyItselfToEmptyLibrary() {
        GateColossus colossus = new GateColossus();
        Card otherCard = new SauroformHybrid();
        harness.setGraveyard(player1, List.of(otherCard, colossus));
        harness.setLibrary(player1, List.of());
        prepareMain(player1);
        harness.setHand(player1, List.of(new RakdosGuildgate()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(colossus);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherCard);
    }

    @Test
    @DisplayName("A Colossus removed from the graveyard before resolution cannot return")
    void removedColossusDoesNotReturn() {
        GateColossus colossus = new GateColossus();
        harness.setGraveyard(player1, List.of(colossus));
        Card topCard = new SauroformHybrid();
        harness.setLibrary(player1, List.of(topCard));
        prepareMain(player1);
        harness.setHand(player1, List.of(new RakdosGuildgate()));
        harness.playLand(player1, 0);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(colossus));

        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.findExiledCard(colossus.getId())).isNotNull();
    }

    private void prepareMain(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

}
