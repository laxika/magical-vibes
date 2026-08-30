package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrumgullyTheGenerous.class, GrizzlyBears.class, FugitiveWizard.class})
class GrumgullyTheGenerousTest extends BaseCardTest {

    @Test
    void otherNonHumanCreatureYouControlEntersWithCounter() {
        harness.addToBattlefield(player1, new GrumgullyTheGenerous());

        castCreature(new GrizzlyBears(), ManaColor.GREEN, ManaColor.GREEN);

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void humanCreatureYouControlDoesNotGetCounter() {
        harness.addToBattlefield(player1, new GrumgullyTheGenerous());

        castCreature(new FugitiveWizard(), ManaColor.BLUE);

        assertThat(findPermanent(player1, "Fugitive Wizard")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentsNonHumanCreatureDoesNotGetCounter() {
        harness.addToBattlefield(player1, new GrumgullyTheGenerous());

        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(findPermanent(player2, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void grumgullyDoesNotGiveItselfCounter() {
        Permanent grumgully = harness.addToBattlefieldAndReturn(player1, new GrumgullyTheGenerous());

        assertThat(grumgully.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castCreature(com.github.laxika.magicalvibes.model.Card creature, ManaColor... mana) {
        harness.setHand(player1, List.of(creature));
        for (ManaColor color : mana) {
            harness.addMana(player1, color, 1);
        }
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
