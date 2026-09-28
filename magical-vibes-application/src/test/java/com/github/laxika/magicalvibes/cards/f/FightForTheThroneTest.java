package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FightForTheThrone.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class})
class FightForTheThroneTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on your creature, fights, and makes you the monarch when the opposing creature dies")
    void fightsAndBecomesMonarchWithCommander() {
        addCommanderToBattlefield();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        castFightForTheThrone(ownCreature, opposingCreature);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Does not make you the monarch when you do not control your commander")
    void doesNotBecomeMonarchWithoutCommander() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        castFightForTheThrone(ownCreature, opposingCreature);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        assertThat(gd.monarchPlayerId).isNull();
    }

    @Test
    @DisplayName("Requires a creature you control and a creature an opponent controls")
    void requiresCorrectTargets() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new FightForTheThrone()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(opposingCreature.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addCommanderToBattlefield() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        harness.addToBattlefield(player1, commander);
    }

    private void castFightForTheThrone(Permanent ownCreature, Permanent opposingCreature) {
        harness.setHand(player1, List.of(new FightForTheThrone()));
        addMana();
        harness.castInstant(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
