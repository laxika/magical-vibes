package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SkySkiff;
import com.github.laxika.magicalvibes.cards.w.WilyGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CanoptekSpyder.class, Forest.class, Ornithopter.class, SkySkiff.class,
        AccordersShield.class, GrizzlyBears.class, WilyGoblin.class})
class CanoptekSpyderTest extends BaseCardTest {

    @Test
    void anotherNontokenArtifactCreatureOrVehicleEnteringDrawsACard() {
        harness.addToBattlefield(player1, new CanoptekSpyder());
        Card creatureDraw = new Forest();
        Card vehicleDraw = new Forest();
        harness.setLibrary(player1, List.of(creatureDraw, vehicleDraw));
        harness.setHand(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new Ornithopter());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new SkySkiff());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creatureDraw, vehicleDraw);
    }

    @Test
    void otherArtifactAndCreatureTypesDoNotTrigger() {
        harness.addToBattlefield(player1, new CanoptekSpyder());
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new AccordersShield());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void tokenArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new CanoptekSpyder());
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new WilyGoblin());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }
}
