package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BurnishedHart;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SadisticShellGame.class, BurnishedHart.class, SolRing.class})
class SadisticShellGameTest extends BaseCardTest {

    @Test
    void startsWithNextOpponentAndDestroysOneChosenCreaturePerPlayer() {
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, new BurnishedHart());
        Permanent player1Artifact = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        Permanent otherPlayer2Creature = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        Permanent player2Artifact = harness.addToBattlefieldAndReturn(player2, new SolRing());

        harness.castFromHand(player1, new SadisticShellGame(), "{4}{B}");
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.playerId()).isEqualTo(player2.getId());
        assertThat(firstChoice.validIds()).containsExactlyInAnyOrder(
                player2Creature.getId(), otherPlayer2Creature.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(player2Creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(player2Creature, otherPlayer2Creature);

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isEqualTo(player1.getId());
        assertThat(secondChoice.validIds()).containsExactlyInAnyOrder(
                player2Creature.getId(), otherPlayer2Creature.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(otherPlayer2Creature.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(player1Creature, player1Artifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(player2Artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(player1Creature.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(
                player2Creature.getCard(), otherPlayer2Creature.getCard());
    }

    @Test
    void bothPlayersCanChooseTheSameCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BurnishedHart());
        harness.castFromHand(player1, new SadisticShellGame(), "{4}{B}");
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player2, List.of(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(creature);
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature.getCard());
    }

    @Test
    void cannotDeclineWhenACreatureCanBeChosen() {
        harness.addToBattlefield(player1, new BurnishedHart());
        harness.addToBattlefield(player2, new BurnishedHart());
        harness.castFromHand(player1, new SadisticShellGame(), "{4}{B}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    void resolvesWithoutChoicesWhenOnlyTheCasterControlsCreatures() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BurnishedHart());
        harness.addToBattlefield(player2, new SolRing());
        harness.castFromHand(player1, new SadisticShellGame(), "{4}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(creature);
        harness.assertOnBattlefield(player2, "Sol Ring");
    }

    @Test
    void resolvesWithoutChoicesWhenNeitherPlayerControlsCreatures() {
        harness.addToBattlefield(player1, new SolRing());
        harness.addToBattlefield(player2, new SolRing());
        harness.castFromHand(player1, new SadisticShellGame(), "{4}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Sol Ring");
        harness.assertOnBattlefield(player2, "Sol Ring");
    }
}
