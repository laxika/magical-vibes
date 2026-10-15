package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.r.RhythmOfTheWild;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EssenceCapture.class, LlanowarElves.class, GrizzlyBears.class, RhythmOfTheWild.class})
class EssenceCaptureTest extends BaseCardTest {

    @Test
    void countersCreatureSpellAndPutsCounterOnControlledCreature() {
        LlanowarElves elves = new LlanowarElves();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new EssenceCapture()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId(), target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mayResolveWithoutOptionalCreatureTarget() {
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new EssenceCapture()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, elves.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    void cannotTargetCreatureNotControlledByCaster() {
        LlanowarElves elves = new LlanowarElves();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new EssenceCapture()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, elves.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotBeCastWithoutCreatureSpellTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new EssenceCapture()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player2, "Essence Capture");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotTargetNoncreatureSpell() {
        LlanowarElves elves = new LlanowarElves();
        EssenceCapture opposingCapture = new EssenceCapture();
        harness.setHand(player1, List.of(elves, new EssenceCapture()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(opposingCapture));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.castInstant(player2, 0, elves.getId());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opposingCapture.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stillPutsCounterWhenCreatureSpellWasAlreadyCountered() {
        LlanowarElves elves = new LlanowarElves();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(elves, new EssenceCapture()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new EssenceCapture()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.castInstant(player2, 0, elves.getId(), target.getId());
        harness.castInstant(player1, 0, elves.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Essence Capture");
    }

    @Test
    void stillCountersSpellWhenCreatureTargetChangesController() {
        LlanowarElves elves = new LlanowarElves();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new EssenceCapture()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.castInstant(player2, 0, elves.getId(), target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void stillPutsCounterWhenCreatureSpellCannotBeCountered() {
        LlanowarElves elves = new LlanowarElves();
        harness.addToBattlefield(player1, new RhythmOfTheWild());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new EssenceCapture()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.castInstant(player2, 0, elves.getId(), target.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(elves.getId());
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
