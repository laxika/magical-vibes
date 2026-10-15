package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VraskasFall.class, GrizzlyBears.class, ChandraNalaar.class, PropheticPrism.class})
class VraskasFallTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent sacrifices a creature and gets a poison counter")
    void sacrificesCreatureAndPoisonsOpponent() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castVraskasFall();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each opponent may sacrifice a planeswalker")
    void sacrificesPlaneswalkerAndPoisonsOpponent() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 5);

        castVraskasFall();

        harness.assertNotOnBattlefield(player2, "Chandra Nalaar");
        harness.assertInGraveyard(player2, "Chandra Nalaar");
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent chooses between a creature and a planeswalker")
    void opponentChoosesPermanentToSacrifice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 5);

        castVraskasFall();

        GameData gameData = harness.getGameData();
        PendingInteraction.MultiPermanentChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(creature.getId(), chandra.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Chandra Nalaar");
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent with no eligible permanent still gets a poison counter")
    void poisonsOpponentWithoutPermanentToSacrifice() {
        castVraskasFall();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }


    @Test
    @DisplayName("The controller keeps their creature and existing poison counters")
    void doesNotAffectController() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        gd.playerPoisonCounters.put(player1.getId(), 2);
        gd.playerPoisonCounters.put(player2.getId(), 3);

        castVraskasFall();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent with only an artifact keeps it and still gets poison")
    void cannotSacrificeNoncreatureArtifact() {
        harness.addToBattlefield(player2, new PropheticPrism());

        castVraskasFall();

        harness.assertOnBattlefield(player2, "Prophetic Prism");
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("The opponent can choose the planeswalker and cannot choose an artifact")
    void choosesPlaneswalkerWhileOtherPermanentsRemain() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 5);
        harness.addToBattlefield(player2, new PropheticPrism());

        castVraskasFall();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(creature.getId(), chandra.getId());
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();

        harness.handleMultiplePermanentsChosen(player2, List.of(chandra.getId()));

        harness.assertInGraveyard(player2, "Chandra Nalaar");
        harness.assertNotOnBattlefield(player2, "Chandra Nalaar");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Prophetic Prism");
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    private void castVraskasFall() {
        harness.setHand(player1, List.of(new VraskasFall()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0);
    }
}
