package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CrystallineGiant;
import com.github.laxika.magicalvibes.cards.d.DelverOfSecrets;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.StonecoilSerpent;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExtinctionEvent.class, GrizzlyBears.class, LlanowarElves.class,
        CrystallineGiant.class, DelverOfSecrets.class, Forest.class, StonecoilSerpent.class})
class ExtinctionEventTest extends BaseCardTest {

    private void castExtinctionEvent() {
        harness.castFromHand(player1, new ExtinctionEvent(), "{3}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolution asks the controller to choose odd or even")
    void asksForParityAtResolution() {
        castExtinctionEvent();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("ODD", "EVEN");
    }

    @Test
    @DisplayName("Exiles matching creatures on every battlefield and leaves other creatures")
    void exilesCreaturesWithChosenParity() {
        Permanent oddOwn = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent evenOwn = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent oddOpponent = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent evenOpponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castExtinctionEvent();
        harness.handleListChoice(player1, "ODD");

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(evenOwn)
                .doesNotContain(oddOwn);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(evenOpponent)
                .doesNotContain(oddOpponent);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(oddOwn.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(oddOpponent.getCard());
    }

    @Test
    @DisplayName("Choosing even exiles creatures with even mana values")
    void exilesEvenManaValueCreatures() {
        Permanent oddOwn = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent evenOwn = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castExtinctionEvent();
        harness.handleListChoice(player1, "EVEN");

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(oddOwn)
                .doesNotContain(evenOwn);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(evenOwn.getCard());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ODD", "EVEN"})
    void xInCreatureManaCostCountsAsZero(String parity) {
        Permanent serpent = harness.addToBattlefieldAndReturn(player2, new StonecoilSerpent());
        serpent.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        castExtinctionEvent();
        harness.handleListChoice(player1, parity);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        if (parity.equals("EVEN")) {
            assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(serpent);
            assertThat(gd.getPlayerExiledCards(player2.getId())).contains(serpent.getCard());
        } else {
            assertThat(gd.playerBattlefields.get(player2.getId())).contains(serpent);
            assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(serpent.getCard());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ODD", "EVEN"})
    void faceDownCreatureCountsAsEven(String parity) {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CrystallineGiant());
        creature.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        castExtinctionEvent();
        harness.handleListChoice(player1, parity);

        if (parity.equals("EVEN")) {
            assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
            assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature.getCard());
        } else {
            assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        }
    }

    @Test
    void exileIgnoresHexproofAndIndestructible() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CrystallineGiant());
        creature.getCounters().put(CounterType.HEXPROOF, 1);
        creature.getCounters().put(CounterType.INDESTRUCTIBLE, 1);

        castExtinctionEvent();
        harness.handleListChoice(player1, "ODD");

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ODD", "EVEN"})
    void transformedCreatureUsesFrontFaceManaValue(String parity) {
        DelverOfSecrets card = new DelverOfSecrets();
        Permanent delver = harness.addToBattlefieldAndReturn(player1, card);
        harness.setLibrary(player1, List.of(new ExtinctionEvent()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(delver.isTransformed()).isTrue();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        castExtinctionEvent();
        harness.handleListChoice(player1, parity);

        if (parity.equals("ODD")) {
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(delver);
            assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        } else {
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(delver);
            assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        }
    }
}
