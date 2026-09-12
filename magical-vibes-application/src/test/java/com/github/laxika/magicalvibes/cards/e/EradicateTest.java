package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.g.GoliathBeetle;
import com.github.laxika.magicalvibes.cards.p.PhyrexianNegator;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Eradicate.class, GoliathBeetle.class, BraidwoodCup.class, PhyrexianNegator.class})
class EradicateTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the target creature and every same-name copy from graveyard, hand, and library")
    void exilesTargetAndAllCopies() {
        harness.addToBattlefield(player2, new GoliathBeetle());
        harness.setHand(player2, List.of(new GoliathBeetle(), new BraidwoodCup()));
        harness.setGraveyard(player2, List.of(new GoliathBeetle(), new BraidwoodCup()));

        GameData gd = harness.getGameData();
        harness.setLibrary(player2, List.of(new GoliathBeetle(), new BraidwoodCup()));

        harness.setHand(player1, List.of(new Eradicate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "Goliath Beetle");
        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Goliath Beetle");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Goliath Beetle"))
                .hasSize(4);

        harness.assertNotInHand(player2, "Goliath Beetle");
        harness.assertNotInGraveyard(player2, "Goliath Beetle");
        harness.assertInHand(player2, "Braidwood Cup");
        harness.assertInGraveyard(player2, "Braidwood Cup");
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Goliath Beetle"))
                .anyMatch(c -> c.getName().equals("Braidwood Cup"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Braidwood Cup"));
    }

    @Test
    @DisplayName("Fizzles if the target creature leaves before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GoliathBeetle());
        harness.setHand(player2, List.of(new GoliathBeetle()));
        harness.setHand(player1, List.of(new Eradicate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID targetId = harness.getPermanentId(player2, "Goliath Beetle");
        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Goliath Beetle");
        harness.assertInGraveyard(player1, "Eradicate");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player2, new PhyrexianNegator());
        harness.setHand(player1, List.of(new Eradicate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID blackId = harness.getPermanentId(player2, "Phyrexian Negator");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, blackId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new BraidwoodCup());
        harness.setHand(player1, List.of(new Eradicate()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID artifactId = harness.getPermanentId(player2, "Braidwood Cup");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifactId))
                .isInstanceOf(IllegalStateException.class);
    }
}
