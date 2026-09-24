package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForceOfDespair.class, GrizzlyBears.class, ScatheZombies.class})
class ForceOfDespairTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys only creatures that entered the battlefield this turn")
    void destroysOnlyCreaturesThatEnteredThisTurn() {
        Permanent olderCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent newCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent newBlackCreature = harness.addToBattlefieldAndReturn(player2, new ScatheZombies());
        gd.permanentsEnteredBattlefieldThisTurn.put(player1.getId(), List.of(newCreature.getCard()));
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(), List.of(newBlackCreature.getCard()));

        harness.setHand(player1, List.of(new ForceOfDespair()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(olderCreature);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Scathe Zombies");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Scathe Zombies");
    }

    @Test
    @DisplayName("Can be cast by exiling a black card from hand during an opponent's turn")
    void castsForAlternateCostDuringOpponentsTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(), List.of(creature.getCard()));
        harness.setHand(player1, List.of(new ForceOfDespair(), new ScatheZombies()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        castWithAlternateExileFromHand(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Force of Despair");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card() instanceof ScatheZombies);
    }

    @Test
    @DisplayName("Alternate cost cannot be used during its controller's turn")
    void alternateCostRequiresOpponentsTurn() {
        harness.setHand(player1, List.of(new ForceOfDespair(), new ScatheZombies()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> castWithAlternateExileFromHand(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castWithAlternateExileFromHand(com.github.laxika.magicalvibes.model.Player player,
                                                 int cardIndex, int exileHandCardIndex) {
        harness.ensurePriority(player);
        gs.playCard(gd, player, cardIndex, 0, null, null, List.of(), List.of(), false,
                null, null, List.of(), null, List.of(), false, exileHandCardIndex);
    }
}
