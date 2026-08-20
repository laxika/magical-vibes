package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BeastWalkers;
import com.github.laxika.magicalvibes.cards.g.GoblinDeathraiders;
import com.github.laxika.magicalvibes.cards.f.FolkOfAnHavva;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AysenCrusader.class, BeastWalkers.class, GoblinDeathraiders.class, FolkOfAnHavva.class})
class AysenCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("With no Soldiers or Warriors, it is 2/2")
    void baseIsTwoTwo() {
        Permanent crusader = addCreatureReady(player1, new AysenCrusader());

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Soldier and Warrior you control adds 1")
    void countsSoldiersAndWarriors() {
        Permanent crusader = addCreatureReady(player1, new AysenCrusader());
        addCreatureReady(player1, new BeastWalkers());
        addCreatureReady(player1, new GoblinDeathraiders());

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creatures that are neither Soldiers nor Warriors are ignored")
    void ignoresOtherCreatureTypes() {
        Permanent crusader = addCreatureReady(player1, new AysenCrusader());
        addCreatureReady(player1, new FolkOfAnHavva());
        addCreatureReady(player1, new FolkOfAnHavva());

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponents' Soldiers and Warriors do not count")
    void ignoresOpponentSoldiers() {
        Permanent crusader = addCreatureReady(player1, new AysenCrusader());
        addCreatureReady(player2, new BeastWalkers());
        addCreatureReady(player2, new GoblinDeathraiders());
        addCreatureReady(player1, new BeastWalkers());

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(3);
    }
}
