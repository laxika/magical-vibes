package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.n.NettleSwine;
import com.github.laxika.magicalvibes.cards.p.PeelFromReality;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MassAppeal.class, MoorlandInquisitor.class, NettleSwine.class, PeelFromReality.class})
class MassAppealTest extends BaseCardTest {

    private void castMassAppeal() {
        harness.setLibrary(player1,
                IntStream.range(0, 6).mapToObj(i -> new NettleSwine()).toList());
        harness.setHand(player1, List.of(new MassAppeal()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Draws a card for each Human the caster controls")
    void drawsOnePerHuman() {
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        harness.addToBattlefield(player1, new MoorlandInquisitor());

        castMassAppeal();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Draws nothing when no Humans are controlled")
    void drawsNothingWithoutHumans() {
        harness.addToBattlefield(player1, new NettleSwine());

        castMassAppeal();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
    }

    @Test
    @DisplayName("Humans controlled by the opponent are not counted")
    void ignoresOpponentHumans() {
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        harness.addToBattlefield(player2, new MoorlandInquisitor());
        harness.addToBattlefield(player2, new MoorlandInquisitor());

        castMassAppeal();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Non-Humans do not increase the draw count alongside Humans")
    void ignoresNonHumansOnMixedBattlefield() {
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        harness.addToBattlefield(player1, new NettleSwine());

        castMassAppeal();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("A Human returned to hand in response is not counted")
    void countsHumansAtResolution() {
        var human = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        var opposingCreature = harness.addToBattlefieldAndReturn(player2, new NettleSwine());
        harness.setLibrary(player1, List.of(new NettleSwine(), new NettleSwine(), new NettleSwine()));
        harness.setHand(player1, List.of(new MassAppeal(), new PeelFromReality()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castSorcery(player1, 0);
        harness.castAndResolveInstant(player1, 0, List.of(human.getId(), opposingCreature.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInHand(player1, "Moorland Inquisitor");
        harness.assertInGraveyard(player1, "Mass Appeal");
    }
}
