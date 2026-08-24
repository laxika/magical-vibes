package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Permanent attacker = addReadyPermanent(player1, new GateColossus(), true);
        Permanent blocker = addReadyPermanent(player2, new GrizzlyBears(), false);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gate Colossus can be blocked by a creature with power three")
    void higherPowerCreatureCanBlock() {
        Permanent attacker = addReadyPermanent(player1, new GateColossus(), true);
        Permanent blocker = addReadyPermanent(player2, creature("Three Power Creature", 3, 3), false);
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
        Card topCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(colossus));
        harness.setLibrary(player1, List.of(topCard));
        prepareMain(player1);

        harness.setHand(player1, List.of(new RakdosGuildgate()));
        harness.castCreature(player1, 0);
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
        harness.castCreature(player1, 0);
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

        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.f.Forest()));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyPermanent(com.github.laxika.magicalvibes.model.Player player, Card card,
                                         boolean attacking) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(attacking);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void prepareMain(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private static Card creature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{3}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
