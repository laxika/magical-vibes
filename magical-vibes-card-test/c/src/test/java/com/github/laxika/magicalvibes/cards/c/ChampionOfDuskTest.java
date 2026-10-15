package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JunglebornPioneer;
import com.github.laxika.magicalvibes.cards.d.DuskLegionZealot;
import com.github.laxika.magicalvibes.cards.u.UniversalAutomaton;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChampionOfDusk.class, DuskLegionZealot.class, JunglebornPioneer.class, Forest.class})
class ChampionOfDuskTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws and loses life equal to the number of Vampires you control")
    void etbDrawsAndLosesLifeForControlledVampires() {
        harness.addToBattlefield(player1, new DuskLegionZealot());
        harness.addToBattlefield(player1, new JunglebornPioneer());
        harness.addToBattlefield(player2, new DuskLegionZealot());
        prepareDeck(2);
        castChampion();

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("ETB counts Champion of Dusk itself as a Vampire")
    void etbCountsItself() {
        prepareDeck(1);
        castChampion();

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("ETB counts changelings as Vampires")
    @CardUsed({UniversalAutomaton.class})
    void etbCountsChangelings() {
        harness.addToBattlefield(player1, new UniversalAutomaton());
        prepareDeck(2);
        castChampion();

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("ETB counts Vampires when the trigger resolves")
    void etbCountsVampiresAddedAfterTriggering() {
        prepareDeck(2);
        castChampion();
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new DuskLegionZealot());

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("ETB draws nothing and loses no life if no Vampires remain")
    void etbWithNoVampiresAtResolution() {
        prepareDeck(1);
        castChampion();
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void castChampion() {
        harness.setHand(player1, List.of(new ChampionOfDusk()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0);
    }

    private void prepareDeck(int forestCount) {
        harness.setLibrary(player1, IntStream.range(0, forestCount)
                .mapToObj(i -> new Forest()).toList());
    }
}
