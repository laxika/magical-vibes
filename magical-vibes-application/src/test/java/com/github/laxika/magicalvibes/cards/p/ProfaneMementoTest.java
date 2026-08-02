package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TomeScour;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProfaneMementoTest extends BaseCardTest {

    @Test
    @DisplayName("Controller gains 1 life when an opponent's creature dies")
    void gainsLifeWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new ProfaneMemento());
        harness.addToBattlefield(player2, new Ornithopter());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID thopterId = harness.getPermanentId(player2, "Ornithopter");
        harness.castInstant(player1, 0, thopterId);
        harness.passBothPriorities(); // Shock resolves, Ornithopter dies, trigger goes on the stack
        harness.passBothPriorities(); // Life-gain trigger resolves

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 1);
    }

    @Test
    @DisplayName("Triggers for each creature card milled into an opponent's graveyard")
    void gainsLifePerMilledCreatureCard() {
        harness.addToBattlefield(player1, new ProfaneMemento());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new TomeScour(),
                new TomeScour(), new TomeScour()));
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Tome Scour resolves, mills 5 → two creature cards
        harness.passBothPriorities(); // first trigger
        harness.passBothPriorities(); // second trigger

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
    }

    @Test
    @DisplayName("Does not trigger when the controller's own creature is put into their graveyard")
    void doesNotTriggerForOwnCreature() {
        harness.addToBattlefield(player1, new ProfaneMemento());
        harness.addToBattlefield(player1, new Ornithopter());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID thopterId = harness.getPermanentId(player1, "Ornithopter");
        harness.castInstant(player1, 0, thopterId);
        harness.passBothPriorities(); // Shock resolves, Ornithopter dies

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Does not trigger for a noncreature card put into an opponent's graveyard")
    void doesNotTriggerForNoncreatureCard() {
        harness.addToBattlefield(player1, new ProfaneMemento());
        harness.setLibrary(player2, List.of(new TomeScour(), new TomeScour(), new TomeScour(),
                new TomeScour(), new TomeScour()));
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new TomeScour()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Tome Scour resolves, mills 5 noncreature cards

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }
}
