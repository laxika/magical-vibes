package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LordOfTheAccursed.class, Gravecrawler.class, GrizzlyBears.class})
class LordOfTheAccursedTest extends BaseCardTest {

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
    @Test
    @DisplayName("Multiple Lords buff each other and the boost ends when one dies")
    void multipleLordsBuffEachOtherUntilOneLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LordOfTheAccursed());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LordOfTheAccursed());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);

        first.setMarkedDamage(4);
        harness.runStateBasedActions();

        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("Menace applies to Zombies present at resolution, excluding later arrivals")
    void menaceUsesZombiesPresentAtResolution() {
        Permanent source = addCreatureReady(player1, new LordOfTheAccursed());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(source.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, source, Keyword.MENACE)).isFalse();
        Permanent beforeResolution = harness.enterBattlefieldAndReturn(player2, new LordOfTheAccursed());
        harness.passBothPriorities();
        Permanent afterResolution = harness.enterBattlefieldAndReturn(player2, new LordOfTheAccursed());

        assertThat(gqs.hasKeyword(gd, source, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("The ability resolves after its source dies and menace persists")
    void abilityResolvesAfterSourceDies() {
        Permanent source = addCreatureReady(player1, new LordOfTheAccursed());
        Permanent opponentZombie = harness.addToBattlefieldAndReturn(player2, new LordOfTheAccursed());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        source.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Lord of the Accursed");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opponentZombie, Keyword.MENACE)).isTrue();
    }
}
