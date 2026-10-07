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
        resolveAllTriggers();

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

    @Test
    @DisplayName("ETB can choose no targets even when a creature is available")
    void entersWithoutReturningCreature() {
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpiderMan2099MiguelOHara()));
        addManaForSpiderMan();

        harness.castCreature(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Spider-Man 2099, Miguel O'Hara");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB can return a creature controlled by its controller")
    void returnsOwnCreature() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        castSpiderMan(target);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Spider-Man's own combat damage draws a card")
    void drawsForItsOwnCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new SpiderMan2099MiguelOHara());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        resolveCombat();
        resolveAllTriggers();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opponent's combat damage does not draw a card")
    void doesNotDrawForOpponentsCombatDamage() {
        harness.addToBattlefield(player1, new SpiderMan2099MiguelOHara());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
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
