package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MaggotCarrier;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RazingSnidd.class, RagingGoblin.class, GrizzlyBears.class, Forest.class, MaggotCarrier.class})
@DisplayName("Razing Snidd")
class RazingSniddTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers only black or red creatures you control, including itself")
    void etbOffersOnlyBlackOrRedCreaturesYouControl() {
        UUID goblinId = harness.addToBattlefieldAndReturn(player1, new RagingGoblin()).getId();
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.addToBattlefield(player2, new RagingGoblin());

        castRazingSnidd();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        UUID sniddId = harness.getPermanentId(player1, "Razing Snidd");
        assertThat(choice.validIds()).containsExactlyInAnyOrder(goblinId, sniddId);
        assertThat(choice.validIds()).doesNotContain(bearsId);
    }

    @Test
    @DisplayName("ETB includes a black creature you control")
    void etbOffersBlackCreatureYouControl() {
        UUID maggotCarrierId = harness.addToBattlefieldAndReturn(player1, new MaggotCarrier()).getId();

        castRazingSnidd();
        harness.passBothPriorities();

        UUID sniddId = harness.getPermanentId(player1, "Razing Snidd");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(maggotCarrierId, sniddId);
    }

    @Test
    @DisplayName("ETB returns the chosen creature and makes each player sacrifice a land")
    void etbReturnsCreatureAndSacrificesLandForEachPlayer() {
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        castRazingSnidd();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Raging Goblin"));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        UUID landId = gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)
                .validIds().getFirst();
        harness.handleMultiplePermanentsChosen(player1, List.of(landId));

        harness.assertInHand(player1, "Raging Goblin");
        harness.assertOnBattlefield(player1, "Razing Snidd");
        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        assertThat(countPermanents(player2, "Forest")).isZero();
    }

    @Test
    @DisplayName("Returning itself does not prevent each player from sacrificing a land")
    void returningItselfDoesNotPreventLandSacrifice() {
        harness.addToBattlefield(player1, new Forest());

        castRazingSnidd();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Razing Snidd"));

        harness.assertInHand(player1, "Razing Snidd");
        harness.assertNotOnBattlefield(player1, "Razing Snidd");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Each player chooses their own land when they control multiple lands")
    void eachPlayerChoosesTheirOwnLand() {
        UUID goblinId = harness.addToBattlefieldAndReturn(player1, new RagingGoblin()).getId();
        UUID player1FirstLandId = harness.addToBattlefieldAndReturn(player1, new Forest()).getId();
        UUID player1SecondLandId = harness.addToBattlefieldAndReturn(player1, new Forest()).getId();
        UUID player2FirstLandId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        UUID player2SecondLandId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();

        castRazingSnidd();
        harness.passBothPriorities();
        UUID sniddId = harness.getPermanentId(player1, "Razing Snidd");
        harness.handlePermanentChosen(player1, goblinId);

        PendingInteraction.MultiPermanentChoice player1Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1Choice.playerId()).isEqualTo(player1.getId());
        assertThat(player1Choice.validIds()).containsExactlyInAnyOrder(player1FirstLandId, player1SecondLandId);
        harness.handleMultiplePermanentsChosen(player1, List.of(player1FirstLandId));

        PendingInteraction.MultiPermanentChoice player2Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2Choice.playerId()).isEqualTo(player2.getId());
        assertThat(player2Choice.validIds()).containsExactlyInAnyOrder(player2FirstLandId, player2SecondLandId);
        harness.handleMultiplePermanentsChosen(player2, List.of(player2FirstLandId));

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(permanent -> permanent.getId()).toList())
                .containsExactlyInAnyOrder(sniddId, player1SecondLandId);
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .map(permanent -> permanent.getId()).toList()).containsExactly(player2SecondLandId);
    }

    private void castRazingSnidd() {
        harness.castFromHand(player1, new RazingSnidd(), "{4}{B}{R}");
        harness.passBothPriorities();
    }

}
