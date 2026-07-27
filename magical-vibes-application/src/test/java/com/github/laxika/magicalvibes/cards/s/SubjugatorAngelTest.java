package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SubjugatorAngelTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps all creatures opponents control")
    void etbTapsOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        harness.setHand(player1, List.of(new SubjugatorAngel()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → ETB triggers
        harness.passBothPriorities(); // resolve ETB

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears")
                        || p.getCard().getName().equals("Llanowar Elves"))
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("ETB does not tap creatures you control")
    void etbDoesNotTapOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        harness.setHand(player1, List.of(new SubjugatorAngel()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent elves = findPermanent(player2, "Llanowar Elves");
        Permanent angel = findPermanent(player1, "Subjugator Angel");

        assertThat(bears.isTapped()).isFalse();
        assertThat(angel.isTapped()).isFalse();
        assertThat(elves.isTapped()).isTrue();
    }
}
