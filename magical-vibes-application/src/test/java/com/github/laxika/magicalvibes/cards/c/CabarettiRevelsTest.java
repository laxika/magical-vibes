package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CabarettiRevels.class, GrizzlyBears.class, LlanowarElves.class, Shock.class})
class CabarettiRevelsTest extends BaseCardTest {

    @Test
    void seeksCreatureWithLesserManaValueOntoBattlefield() {
        harness.addToBattlefield(player1, new CabarettiRevels());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    void doesNotTriggerForNoncreatureSpells() {
        harness.addToBattlefield(player1, new CabarettiRevels());
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Llanowar Elves");
    }

    @Test
    void doesNotPutCreatureOntoBattlefieldWhenNoLesserCreatureExists() {
        harness.addToBattlefield(player1, new CabarettiRevels());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears")))
                .hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }
}
