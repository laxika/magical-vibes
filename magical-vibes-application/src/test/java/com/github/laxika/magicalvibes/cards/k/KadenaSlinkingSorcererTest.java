package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AinokTracker;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KadenaSlinkingSorcerer.class, AinokTracker.class, Forest.class, GrizzlyBears.class})
class KadenaSlinkingSorcererTest extends BaseCardTest {

    @Test
    void firstFaceDownCreatureSpellEachTurnCostsThreeLess() {
        addKadena();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new AinokTracker(), new AinokTracker()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);

        assertThatThrownBy(() -> harness.castCreatureWithMorph(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void faceUpCreatureSpellDoesNotConsumeFaceDownReduction() {
        addKadena();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears(), new AinokTracker()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreatureWithMorph(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void drawsWhenAFaceDownCreatureEntersUnderYourControl() {
        KadenaSlinkingSorcerer kadena = new KadenaSlinkingSorcerer();
        harness.addToBattlefield(player1, kadena);
        Forest draw = new Forest();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of(new AinokTracker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    private void addKadena() {
        harness.addToBattlefield(player1, new KadenaSlinkingSorcerer());
    }
}
