package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Carom;
import com.github.laxika.magicalvibes.cards.e.Electrolyze;
import com.github.laxika.magicalvibes.cards.g.GruulNodorog;
import com.github.laxika.magicalvibes.cards.g.GruulScrapper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulsOfTheFaultless.class, GruulScrapper.class, GruulNodorog.class, Electrolyze.class, Carom.class})
class SoulsOfTheFaultlessTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage causes its controller to gain life and the attacker to lose life")
    void gainsLifeAndAttackerLosesLifeFromCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new GruulScrapper());
        attacker.setAttacking(true);

        Permanent souls = addCreatureReady(player2, new SoulsOfTheFaultless());
        souls.setBlocking(true);
        souls.addBlockingTarget(0);

        resolveCombat(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 23);
        harness.assertOnBattlefield(player2, "Souls of the Faultless");
    }

    @Test
    @DisplayName("The trigger resolves even when combat damage is lethal")
    void resolvesAfterSoulsDiesToCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new GruulNodorog());
        attacker.setAttacking(true);

        Permanent souls = addCreatureReady(player2, new SoulsOfTheFaultless());
        souls.setBlocking(true);
        souls.addBlockingTarget(0);

        resolveCombat(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 24);
        harness.assertInGraveyard(player2, "Souls of the Faultless");
        harness.assertNotOnBattlefield(player2, "Souls of the Faultless");
    }

    @Test
    @DisplayName("Noncombat damage does not trigger Souls of the Faultless")
    void ignoresNoncombatDamage() {
        Permanent souls = addCreatureReady(player2, new SoulsOfTheFaultless());
        harness.setHand(player1, List.of(new Electrolyze()));
        harness.setLibrary(player1, List.of(new SoulsOfTheFaultless()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, Map.of(souls.getId(), 2));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
        assertThat(souls.getMarkedDamage()).isEqualTo(2);
        harness.assertInHand(player1, "Souls of the Faultless");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Redirected blocker combat damage makes the attacking player lose life")
    void redirectedBlockerDamageMakesAttackingPlayerLoseLife() {
        Permanent attacker = addCreatureReady(player1, new GruulNodorog());
        Permanent blocker = addCreatureReady(player2, new GruulScrapper());
        Permanent souls = addCreatureReady(player2, new SoulsOfTheFaultless());
        harness.setHand(player2, List.of(new Carom()));
        harness.setLibrary(player2, List.of(new SoulsOfTheFaultless()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, List.of(attacker.getId(), souls.getId()));

        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(souls.getMarkedDamage()).isEqualTo(1);
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 21);
        harness.assertInGraveyard(player2, "Gruul Scrapper");
    }
}
