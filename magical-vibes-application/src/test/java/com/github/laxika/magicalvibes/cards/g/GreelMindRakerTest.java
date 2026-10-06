package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GreelMindRaker.class, Forest.class, GrizzlyBears.class, LightningBolt.class,
        GiantGrowth.class, SerraAngel.class})
class GreelMindRakerTest extends BaseCardTest {

    private Permanent readyGreel() {
        Permanent greel = addCreatureReady(player1, new GreelMindRaker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return greel;
    }

    @Test
    @DisplayName("Discards two cards as a cost, then makes the target discard X cards at random")
    void discardsCostAndRandomTargetDiscard() {
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new LightningBolt(), new GiantGrowth(), new Forest(), new SerraAngel()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        Permanent greel = readyGreel();

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(greel.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("X=0 still requires two cards but makes the target discard nothing")
    void zeroXStillPaysDiscardCost() {
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new LightningBolt(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        readyGreel();

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetController() {
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        readyGreel();

        harness.activateAbility(player1, 0, 1, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Cannot activate without two cards to discard")
    void cannotActivateWithoutTwoCardsToDiscard() {
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        Permanent greel = readyGreel();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(greel.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Discards the target's entire hand when X exceeds its hand size")
    void xGreaterThanTargetHandDiscardsEntireHand() {
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new SerraAngel()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        readyGreel();

        harness.activateAbility(player1, 0, 3, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        Permanent greel = readyGreel();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, greel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player");
    }

    @Test
    @DisplayName("An empty target hand does not prevent activation or paying the costs")
    void canTargetPlayerWithEmptyHand() {
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 3);
        Permanent greel = readyGreel();

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(greel.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWithSummoningSickness() {
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        Permanent greel = readyGreel();
        greel.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(greel.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A tapped Greel cannot activate")
    void cannotActivateWhenTapped() {
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        Permanent greel = readyGreel();
        greel.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability resolves after Greel dies, with the activation costs already paid")
    void abilityResolvesAfterSourceDies() {
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player2, List.of(new LightningBolt(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.RED, 1);
        Permanent greel = readyGreel();

        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(greel.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);

        harness.castInstant(player2, 0, greel.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Greel, Mind Raker");
        harness.assertInGraveyard(player1, "Greel, Mind Raker");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }
}
