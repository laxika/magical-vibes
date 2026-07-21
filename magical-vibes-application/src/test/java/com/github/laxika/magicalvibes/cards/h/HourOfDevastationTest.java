package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.cards.w.WithstandDeath;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HourOfDevastationTest extends BaseCardTest {

    private void castAndResolve() {
        harness.setHand(player1, List.of(new HourOfDevastation()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Deals 5 damage to each creature, killing 2/2s on both sides")
    void dealsFiveToEachCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAndResolve();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Strips indestructible so lethal damage destroys the creature")
    void stripsIndestructibleThenDamages() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WithstandDeath()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();

        castAndResolve();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Deals 5 damage to a non-Bolas planeswalker")
    void damagesNonBolasPlaneswalker() {
        Permanent chandra = new Permanent(new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 5);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(chandra);

        castAndResolve();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Chandra Nalaar"));
    }

    @Test
    @DisplayName("Does not damage a Bolas planeswalker")
    void sparesBolasPlaneswalker() {
        Permanent bolas = new Permanent(new NicolBolasPlaneswalker());
        bolas.setCounterCount(CounterType.LOYALTY, 5);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bolas);

        castAndResolve();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Nicol Bolas, Planeswalker"));
        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not deal damage to players")
    void doesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castAndResolve();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
