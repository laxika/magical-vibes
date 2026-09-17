package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiderMan2099MiguelOHara.class, GrizzlyBears.class, Forest.class})
class SpiderMan2099MiguelOHaraTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns up to one target creature to its owner's hand")
    void entersAndReturnsTargetCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castSpiderMan(target);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Draws one card when one or more controlled creatures deal combat damage")
    void drawsOnceForMultipleCombatDamageDealers() {
        harness.addToBattlefield(player1, new SpiderMan2099MiguelOHara());
        addAttacker();
        addAttacker();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void castSpiderMan(Permanent target) {
        harness.setHand(player1, List.of(new SpiderMan2099MiguelOHara()));
        addManaForSpiderMan();
        harness.castCreature(player1, 0, List.of(target.getId()));
    }

    private void addManaForSpiderMan() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void addAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
    }
}
