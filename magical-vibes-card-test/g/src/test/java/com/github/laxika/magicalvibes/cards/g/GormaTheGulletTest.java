package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DragonFodder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GormaTheGullet.class, GrizzlyBears.class, Shock.class, DragonFodder.class})
class GormaTheGulletTest extends BaseCardTest {

    @Test
    @DisplayName("Gains counters for ally deaths and gives them to later nontoken creatures")
    void tracksAllyDeathsForCountersAndCreatureEntries() {
        Permanent gorma = harness.addToBattlefieldAndReturn(player1, new GormaTheGullet());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killWithShock(victim);

        assertThat(gorma.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.setHand(player1, List.of(new DragonFodder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin"))
                .hasSize(2)
                .allSatisfy(goblin -> assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());

        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ignores creatures that died under an opponent's control")
    void ignoresOpponentCreatureDeaths() {
        Permanent gorma = harness.addToBattlefieldAndReturn(player1, new GormaTheGullet());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithShock(victim);

        assertThat(gorma.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Counts earlier deaths without applying its general entry effect to itself")
    void countsDeathsBeforeGormaEntered() {
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        killWithShock(victim);

        Permanent gorma = harness.enterBattlefieldAndReturn(player1, new GormaTheGullet());
        assertThat(gorma.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Token deaths each grow Gorma and count toward nontoken entries")
    void countsTokenDeathsIndividually() {
        Permanent gorma = harness.addToBattlefieldAndReturn(player1, new GormaTheGullet());
        harness.setHand(player1, List.of(new DragonFodder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        List<Permanent> goblins = findPermanents(player1, "Goblin");
        assertThat(goblins).hasSize(2);
        killWithShock(goblins.get(0));
        killWithShock(goblins.get(1));

        assertThat(gorma.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not give entry counters to opposing creatures")
    void doesNotModifyOpponentEntries() {
        harness.addToBattlefield(player1, new GormaTheGullet());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        killWithShock(victim);

        Permanent entering = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Entry counters stop when Gorma leaves the battlefield")
    void doesNotModifyEntriesAfterGormaDies() {
        Permanent gorma = harness.addToBattlefieldAndReturn(player1, new GormaTheGullet());
        killWithShock(gorma);

        assertThat(findPermanents(player1, "Gorma, the Gullet")).isEmpty();
        assertThat(gd.stack).isEmpty();
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Entry counters use only deaths from the current turn")
    void resetsEntryDeathCountNextTurn() {
        Permanent gorma = harness.addToBattlefieldAndReturn(player1, new GormaTheGullet());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        killWithShock(victim);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gorma.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(entering.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void killWithShock(Permanent creature) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
