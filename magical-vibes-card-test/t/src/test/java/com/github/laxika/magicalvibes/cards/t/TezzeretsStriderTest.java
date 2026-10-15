package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AjaniAdversaryOfTyrants;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TezzeretsStrider.class, TezzeretCruelMachinist.class, AjaniAdversaryOfTyrants.class,
        GreenwoodSentinel.class, Disperse.class})
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
        harness.addToBattlefield(player1, new AjaniAdversaryOfTyrants());

        assertThat(gqs.hasKeyword(gd, strider, Keyword.MENACE)).isFalse();
    }

    @Test
    void nonPlaneswalkerWithTezzeretSubtypeDoesNotGrantMenace() {
        Permanent strider = addCreatureReady(player1, new TezzeretsStrider());
        Card creature = new GreenwoodSentinel();
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

    @Test
    void losesMenaceWhenTezzeretLeavesTheBattlefield() {
        Permanent strider = addCreatureReady(player1, new TezzeretsStrider());
        Permanent tezzeret = harness.addToBattlefieldAndReturn(player1, new TezzeretCruelMachinist());
        assertThat(gqs.hasKeyword(gd, strider, Keyword.MENACE)).isTrue();

        harness.setHand(player1, List.of(new Disperse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, tezzeret.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tezzeret, Cruel Machinist");
        harness.assertInHand(player1, "Tezzeret, Cruel Machinist");
        assertThat(gqs.hasKeyword(gd, strider, Keyword.MENACE)).isFalse();
    }

    @Test
    void menaceTracksTheStridersCurrentController() {
        Permanent strider = addCreatureReady(player1, new TezzeretsStrider());
        harness.addToBattlefield(player1, new TezzeretCruelMachinist());
        assertThat(gqs.hasKeyword(gd, strider, Keyword.MENACE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(strider);
        gd.playerBattlefields.get(player2.getId()).add(strider);

        assertThat(gqs.hasKeyword(gd, strider, Keyword.MENACE)).isFalse();

        harness.addToBattlefield(player2, new TezzeretCruelMachinist());

        assertThat(gqs.hasKeyword(gd, strider, Keyword.MENACE)).isTrue();
    }

    @Test
    void doesNotGrantMenaceToOtherCreatures() {
        Permanent strider = addCreatureReady(player1, new TezzeretsStrider());
        Permanent otherCreature = addCreatureReady(player1, new GreenwoodSentinel());
        harness.addToBattlefield(player1, new TezzeretCruelMachinist());

        assertThat(gqs.hasKeyword(gd, strider, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.MENACE)).isFalse();
    }

    @Test
    void cannotBeBlockedByOneCreatureWhileControllingTezzeret() {
        addCreatureReady(player1, new TezzeretsStrider());
        harness.addToBattlefield(player1, new TezzeretCruelMachinist());
        addCreatureReady(player2, new GreenwoodSentinel());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void canBeBlockedByTwoCreaturesWhileControllingTezzeret() {
        addCreatureReady(player1, new TezzeretsStrider());
        harness.addToBattlefield(player1, new TezzeretCruelMachinist());
        Permanent firstBlocker = addCreatureReady(player2, new GreenwoodSentinel());
        Permanent secondBlocker = addCreatureReady(player2, new GreenwoodSentinel());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    void canBeBlockedByOneCreatureWithoutTezzeret() {
        addCreatureReady(player1, new TezzeretsStrider());
        Permanent blocker = addCreatureReady(player2, new GreenwoodSentinel());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
