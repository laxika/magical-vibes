package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BarterInBloodTest extends BaseCardTest {

    private void castBarter() {
        harness.setHand(player1, List.of(new BarterInBlood()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private List<UUID> creatureIds(com.github.laxika.magicalvibes.model.Player player) {
        return harness.getGameData().playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.CREATURE))
                .map(Permanent::getId)
                .toList();
    }

    private long creatureCount(com.github.laxika.magicalvibes.model.Player player) {
        return harness.getGameData().playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.CREATURE))
                .count();
    }

    @Test
    @DisplayName("Each player with exactly two creatures loses both without a prompt")
    void bothPlayersWithTwoCreaturesLoseBoth() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castBarter();

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        assertThat(creatureCount(player1)).isZero();
        assertThat(creatureCount(player2)).isZero();
    }

    @Test
    @DisplayName("A player with only one creature sacrifices just that one")
    void playerWithOneCreatureSacrificesIt() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castBarter();

        assertThat(creatureCount(player1)).isZero();
        assertThat(creatureCount(player2)).isZero();
    }

    @Test
    @DisplayName("Only creatures are sacrificed — lands are untouched")
    void landsAreNotSacrificed() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Mountain());

        castBarter();

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(creatureCount(player1)).isZero();
    }

    @Test
    @DisplayName("A player with three or more creatures chooses which two to sacrifice")
    void playerWithMoreThanTwoCreaturesChooses() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
        // Player2 has exactly two — auto-marked, deferred until player1 has chosen
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castBarter();

        GameData gd = harness.getGameData();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        // Player2's two creatures are deferred for the simultaneous sacrifice (CR 101.4)
        assertThat(((MultiPermanentChoiceContext.ForcedSacrifice) choice.context()).accumulatedSacrificeIds())
                .hasSize(2);
        assertThat(creatureCount(player2)).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, creatureIds(player1).stream().limit(2).toList());

        assertThat(creatureCount(player1)).isEqualTo(2);
        assertThat(creatureCount(player2)).isZero();
    }

    @Test
    @DisplayName("Both players choose in APNAP order and everything is sacrificed simultaneously")
    void bothPlayersChooseSequentially() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }

        castBarter();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMultiplePermanentsChosen(player1, creatureIds(player1).stream().limit(2).toList());

        // Nothing sacrificed yet — player1's picks wait for player2's choice
        assertThat(creatureCount(player1)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMultiplePermanentsChosen(player2, creatureIds(player2).stream().limit(2).toList());

        assertThat(creatureCount(player1)).isEqualTo(1);
        assertThat(creatureCount(player2)).isEqualTo(2);
    }

    @Test
    @DisplayName("A player with no creatures is unaffected")
    void playerWithNoCreaturesIsUnaffected() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castBarter();

        assertThat(creatureCount(player1)).isZero();
        assertThat(creatureCount(player2)).isZero();
    }
}
