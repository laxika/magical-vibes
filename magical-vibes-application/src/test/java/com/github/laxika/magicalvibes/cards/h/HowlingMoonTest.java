package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.RecklessWaif;
import com.github.laxika.magicalvibes.cards.y.YoungWolf;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HowlingMoon.class, YoungWolf.class, RecklessWaif.class, GrizzlyBears.class, LightningBolt.class})
class HowlingMoonTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a target Wolf or Werewolf at the beginning of combat")
    void boostsTargetWolfOrWerewolf() {
        harness.addToBattlefield(player1, new HowlingMoon());
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new YoungWolf());
        Permanent werewolf = harness.addToBattlefieldAndReturn(player1, new RecklessWaif());
        int initialPower = gqs.getEffectivePower(gd, werewolf);
        int initialToughness = gqs.getEffectiveToughness(gd, werewolf);

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(wolf.getId(), werewolf.getId());

        harness.handlePermanentChosen(player1, werewolf.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, werewolf)).isEqualTo(initialPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, werewolf)).isEqualTo(initialToughness + 2);
    }

    @Test
    @DisplayName("Cannot target a non-Wolf or a Wolf controlled by an opponent")
    void cannotTargetInvalidCreature() {
        harness.addToBattlefield(player1, new HowlingMoon());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opponentWolf = harness.addToBattlefieldAndReturn(player2, new YoungWolf());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1,
                harness.getPermanentId(player1, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentWolf.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new HowlingMoon());
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new YoungWolf());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, wolf.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(3);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates a 2/2 green Wolf when an opponent casts their second spell")
    void createsWolfOnOpponentsSecondSpell() {
        harness.addToBattlefield(player1, new HowlingMoon());
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Wolf")).isZero();

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);
        Permanent wolf = findPermanent(player1, "Wolf");
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
        assertThat(wolf.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(wolf.getCard().getSubtypes()).contains(CardSubtype.WOLF);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when you cast your second spell")
    void doesNotTriggerForControllersSecondSpell() {
        harness.addToBattlefield(player1, new HowlingMoon());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Wolf")).isZero();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
