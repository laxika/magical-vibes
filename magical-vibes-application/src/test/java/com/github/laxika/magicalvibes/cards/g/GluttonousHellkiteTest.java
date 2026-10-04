package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GluttonousHellkite.class, SakuraTribeElder.class})
class GluttonousHellkiteTest extends BaseCardTest {

    @Test
    @DisplayName("Each player sacrifices X creatures, then Hellkite enters with two counters per creature")
    void sacrificesXCreaturesAndEntersWithCounters() {
        harness.addToBattlefield(player1, new SakuraTribeElder());
        harness.addToBattlefield(player2, new SakuraTribeElder());
        harness.setHand(player1, List.of(new GluttonousHellkite()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sakura-Tribe Elder");
        harness.assertNotOnBattlefield(player2, "Sakura-Tribe Elder");
        Permanent hellkite = findPermanent(player1, "Gluttonous Hellkite");
        assertThat(hellkite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void zeroXDoesNotSacrificeCreaturesOrGrantCounters() {
        harness.addToBattlefield(player1, new SakuraTribeElder());
        harness.addToBattlefield(player2, new SakuraTribeElder());

        castHellkite(0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sakura-Tribe Elder");
        harness.assertOnBattlefield(player2, "Sakura-Tribe Elder");
        assertThat(findPermanent(player1, "Gluttonous Hellkite")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void countsOnlyActualSacrificesWhenPlayersHaveFewerThanXCreatures() {
        harness.addToBattlefield(player2, new SakuraTribeElder());

        castHellkite(2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Sakura-Tribe Elder");
        harness.assertNotOnBattlefield(player1, "Gluttonous Hellkite");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Gluttonous Hellkite")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void eachPlayerChoosesAndSacrificesAreDeferredUntilBothHaveChosen() {
        harness.addToBattlefield(player1, new SakuraTribeElder());
        harness.addToBattlefield(player1, new SakuraTribeElder());
        harness.addToBattlefield(player2, new SakuraTribeElder());
        harness.addToBattlefield(player2, new SakuraTribeElder());
        Permanent firstChoice = findPermanent(player1, "Sakura-Tribe Elder");
        Permanent secondChoice = findPermanent(player2, "Sakura-Tribe Elder");

        castHellkite(1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(firstChoice.getId()));

        assertThat(countPermanents(player1, "Sakura-Tribe Elder")).isEqualTo(2);
        assertThat(countPermanents(player2, "Sakura-Tribe Elder")).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player2, List.of(secondChoice.getId()));

        assertThat(findPermanents(player1, "Sakura-Tribe Elder")).doesNotContain(firstChoice);
        assertThat(findPermanents(player2, "Sakura-Tribe Elder")).doesNotContain(secondChoice);
        assertThat(countPermanents(player1, "Sakura-Tribe Elder")).isEqualTo(1);
        assertThat(countPermanents(player2, "Sakura-Tribe Elder")).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Gluttonous Hellkite")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void positiveXWithNoCreaturesGrantsNoCounters() {
        castHellkite(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Gluttonous Hellkite")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void enteringWithoutBeingCastDoesNotSacrificeOrGrantCounters() {
        harness.addToBattlefield(player1, new SakuraTribeElder());
        harness.addToBattlefield(player2, new SakuraTribeElder());

        Permanent hellkite = harness.enterBattlefieldAndReturn(player1, new GluttonousHellkite());

        harness.assertOnBattlefield(player1, "Sakura-Tribe Elder");
        harness.assertOnBattlefield(player2, "Sakura-Tribe Elder");
        assertThat(hellkite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void castHellkite(int x) {
        harness.setHand(player1, List.of(new GluttonousHellkite()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2 * x);
        harness.castCreature(player1, 0, x);
    }
}
