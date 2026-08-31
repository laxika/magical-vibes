package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RenataCalledToTheHunt.class, GrizzlyBears.class, LlanowarElves.class})
class RenataCalledToTheHuntTest extends BaseCardTest {

    @Test
    @DisplayName("Renata's power equals green devotion and her toughness stays 3")
    void powerEqualsGreenDevotion() {
        Permanent renata = addRenata();

        assertThat(gqs.getEffectivePower(gd, renata)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, renata)).isEqualTo(3);

        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, renata)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, renata)).isEqualTo(3);
    }

    @Test
    @DisplayName("Renata's power updates when a green permanent leaves your battlefield")
    void powerUpdatesWhenGreenPermanentLeaves() {
        Permanent renata = addRenata();
        harness.addToBattlefield(player1, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, renata)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Llanowar Elves"));

        assertThat(gqs.getEffectivePower(gd, renata)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each other creature you control enters with a +1/+1 counter")
    void otherCreatureEntersWithCounter() {
        addRenata();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Renata does not give herself a counter when she enters")
    void ownEntryDoesNotGetCounter() {
        Permanent renata = addRenata();

        assertThat(renata.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's creature does not get a counter")
    void opponentCreatureDoesNotGetCounter() {
        addRenata();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addRenata() {
        return harness.addToBattlefieldAndReturn(player1, new RenataCalledToTheHunt());
    }
}
