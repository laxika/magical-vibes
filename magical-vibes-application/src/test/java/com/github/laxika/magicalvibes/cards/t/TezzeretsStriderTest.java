package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AjaniGoldmane;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TezzeretsStriderTest extends BaseCardTest {

    @Test
    void hasNoMenaceWithoutTezzeretPlaneswalker() {
        Permanent strider = addCreatureReady(player1, new TezzeretsStrider());

        assertThat(gqs.hasKeyword(gd, strider, Keyword.MENACE)).isFalse();
    }

    @Test
    void gainsMenaceWithTezzeretPlaneswalker() {
        Permanent strider = addCreatureReady(player1, new TezzeretsStrider());
        harness.addToBattlefield(player1, new TezzeretCruelMachinist());

        assertThat(gqs.hasKeyword(gd, strider, Keyword.MENACE)).isTrue();
    }

    @Test
    void otherPlaneswalkerDoesNotGrantMenace() {
        Permanent strider = addCreatureReady(player1, new TezzeretsStrider());
        harness.addToBattlefield(player1, new AjaniGoldmane());

        assertThat(gqs.hasKeyword(gd, strider, Keyword.MENACE)).isFalse();
    }

    @Test
    void nonPlaneswalkerWithTezzeretSubtypeDoesNotGrantMenace() {
        Permanent strider = addCreatureReady(player1, new TezzeretsStrider());
        Card creature = new GrizzlyBears();
        creature.setSubtypes(List.of(CardSubtype.TEZZERET));
        harness.addToBattlefield(player1, creature);

        assertThat(gqs.hasKeyword(gd, strider, Keyword.MENACE)).isFalse();
    }

    @Test
    void opponentTezzeretDoesNotGrantMenace() {
        Permanent strider = addCreatureReady(player1, new TezzeretsStrider());
        harness.addToBattlefield(player2, new TezzeretCruelMachinist());

        assertThat(gqs.hasKeyword(gd, strider, Keyword.MENACE)).isFalse();
    }
}
