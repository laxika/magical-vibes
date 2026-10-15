package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BugenhagenWiseElder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YunasWhistle.class, Forest.class, BugenhagenWiseElder.class})
class YunasWhistleTest extends BaseCardTest {

    @Test
    void putsTheCreatureIntoHandBottomsTheRestAndAddsCountersByManaValue() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BugenhagenWiseElder());
        Card forest = new Forest();
        Card creature = new BugenhagenWiseElder();
        Card unrevealed = new Forest();
        harness.setLibrary(player1, List.of(forest, creature, unrevealed));
        harness.setHand(player1, List.of(new YunasWhistle()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealed, forest);
    }

    @Test
    void addsNoCountersWhenTheLibraryHasNoCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BugenhagenWiseElder());
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(new YunasWhistle()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetAnOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BugenhagenWiseElder());
        harness.addToBattlefield(player1, new BugenhagenWiseElder());
        harness.setLibrary(player1, List.of(new BugenhagenWiseElder()));
        harness.setHand(player1, List.of(new YunasWhistle()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void findsACreatureEvenWhenYouControlNoCreatures() {
        Card creature = new BugenhagenWiseElder();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest, creature));
        harness.setHand(player1, List.of(new YunasWhistle()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyLibraryCreatesNoCounterTrigger() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new YunasWhistle()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
