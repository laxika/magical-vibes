package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BrindleBoar;
import com.github.laxika.magicalvibes.cards.r.RegathanFirecat;
import com.github.laxika.magicalvibes.cards.s.SliverConstruct;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AcademyRaider.class, Forest.class, BrindleBoar.class, RegathanFirecat.class, SliverConstruct.class})
class AcademyRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player lets the controller discard a card to draw a card")
    void acceptMayDiscardsThenDraws() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new BrindleBoar()));
        harness.setLife(player2, 20);

        attackWithRaiderDealingDamage();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Brindle Boar");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Declining the trigger neither discards nor draws")
    void declineMayDoesNothing() {
        harness.setHand(player1, List.of(new BrindleBoar()));
        harness.setLife(player2, 20);

        attackWithRaiderDealingDamage();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Brindle Boar");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Accepting with an empty hand discards nothing and draws nothing")
    void acceptMayWithEmptyHandDoesNothing() {
        harness.setHand(player1, List.of());
        harness.setLife(player2, 20);

        attackWithRaiderDealingDamage();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Intimidate prevents a green nonartifact creature from blocking")
    void greenCreatureCannotBlock() {
        Permanent raider = addCreatureReady(player1, new AcademyRaider());
        addCreatureReady(player2, new BrindleBoar());
        raider.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("intimidate");
    }

    @Test
    @DisplayName("A red creature can block, and damage to it does not trigger rummaging")
    void redCreatureCanBlockWithoutRummageTrigger() {
        Permanent raider = addCreatureReady(player1, new AcademyRaider());
        Permanent blocker = addCreatureReady(player2, new RegathanFirecat());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player2, 20);
        raider.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Academy Raider");
        harness.assertInGraveyard(player2, "Regathan Firecat");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A colorless artifact creature can block despite sharing no color")
    void artifactCreatureCanBlock() {
        Permanent raider = addCreatureReady(player1, new AcademyRaider());
        Permanent blocker = addCreatureReady(player2, new SliverConstruct());
        raider.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void attackWithRaiderDealingDamage() {
        Permanent raider = addCreatureReady(player1, new AcademyRaider());
        raider.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
