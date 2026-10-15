package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpectralDeluge.class, GrizzlyBears.class, HillGiant.class, Island.class})
class SpectralDelugeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns opposing creatures with toughness at most the number of Islands you control")
    void returnsOpposingCreaturesWithinIslandThreshold() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        castSpectralDeluge();

        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Determines the Island count when the spell resolves")
    void determinesIslandCountAtResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpectralDeluge()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0);

        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not return opposing creatures when you control no Islands")
    void doesNotReturnCreaturesWithoutIslands() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castSpectralDeluge();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can be foretold and cast for {1}{U}{U} on a later turn")
    void foretellsAndCastsOnLaterTurn() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new GrizzlyBears());
        SpectralDeluge deluge = new SpectralDeluge();
        harness.setHand(player1, List.of(deluge));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(deluge.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromExile(player1, deluge.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
    }

    private void castSpectralDeluge() {
        harness.setHand(player1, List.of(new SpectralDeluge()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0);
    }
}
