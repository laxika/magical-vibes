package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HallowedBurialTest extends BaseCardTest {

    @Test
    @DisplayName("Puts all creatures on the bottom of their owners' libraries")
    void bottomsAllCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        harness.setHand(player1, List.of(new HallowedBurial()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");

        // Cards go to the bottom of their owners' libraries, not the graveyard
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Indestructible does not save a creature from Hallowed Burial")
    void indestructibleDoesNotSave() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        harness.setHand(player1, List.of(new HallowedBurial()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Not a destroy effect — indestructible is irrelevant
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does nothing when no creatures are on the battlefield")
    void doesNothingWhenNoCreatures() {
        harness.setHand(player1, List.of(new HallowedBurial()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Hallowed Burial");
    }
}
