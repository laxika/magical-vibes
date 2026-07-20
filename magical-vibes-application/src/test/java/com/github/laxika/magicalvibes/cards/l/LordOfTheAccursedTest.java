package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LordOfTheAccursedTest extends BaseCardTest {

    // ===== Static effect: other Zombies you control get +1/+1 =====

    @Test
    @DisplayName("Other Zombies you control get +1/+1")
    void buffsOtherZombiesYouControl() {
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.addToBattlefield(player1, new LordOfTheAccursed());

        Permanent zombie = findPermanent(player1, "Gravecrawler");

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(2);
    }

    @Test
    @DisplayName("Lord of the Accursed does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new LordOfTheAccursed());

        Permanent lord = findPermanent(player1, "Lord of the Accursed");

        assertThat(gqs.getEffectivePower(gd, lord)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lord)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff non-Zombie creatures")
    void doesNotBuffNonZombies() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LordOfTheAccursed());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Zombies")
    void doesNotBuffOpponentZombies() {
        harness.addToBattlefield(player1, new LordOfTheAccursed());
        harness.addToBattlefield(player2, new Gravecrawler());

        Permanent opponentZombie = findPermanent(player2, "Gravecrawler");

        assertThat(gqs.getEffectivePower(gd, opponentZombie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentZombie)).isEqualTo(1);
    }

    // ===== Activated ability: all Zombies gain menace until end of turn =====

    @Test
    @DisplayName("Activated ability grants menace to all Zombies (both controllers)")
    void grantsMenaceToAllZombies() {
        Permanent lord = addCreatureReady(player1, new LordOfTheAccursed());
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Gravecrawler());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, lord, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Gravecrawler"), Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Gravecrawler"), Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Granted menace wears off at end of turn")
    void menaceWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new LordOfTheAccursed());
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent zombie = findPermanent(player1, "Gravecrawler");
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, zombie, Keyword.MENACE)).isFalse();
    }
}
