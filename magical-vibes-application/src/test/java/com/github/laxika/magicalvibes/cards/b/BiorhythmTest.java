package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DaruLancer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({Biorhythm.class, DaruLancer.class, Forest.class})
class BiorhythmTest extends BaseCardTest {

    @Test
    @DisplayName("Each player's life total becomes the number of creatures they control")
    void setsLifeToCreatureCount() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new DaruLancer());
        harness.addToBattlefield(player1, new DaruLancer());
        harness.addToBattlefield(player2, new DaruLancer());
        harness.setHand(player1, List.of(new Biorhythm()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 2);
        harness.assertLife(player2, 1);
    }

    @Test
    @DisplayName("A player controlling no creatures has their life total set to 0")
    void setsLifeToZeroWithNoCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new DaruLancer());
        harness.setHand(player1, List.of(new Biorhythm()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 1);
        harness.assertLife(player2, 0);
    }

    @Test
    @DisplayName("Does not count noncreature permanents")
    void ignoresNoncreaturePermanents() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new DaruLancer());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Biorhythm()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 1);
        harness.assertLife(player2, 0);
    }
}
