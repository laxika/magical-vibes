package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeanceBoard.class, GrizzlyBears.class, LightningBolt.class, Shock.class, PropheticPrism.class})
class SeanceBoardTest extends BaseCardTest {

    @Test
    void morbidPutsASoulCounterOnTheBoardAtEndStep() {
        Permanent board = addCreatureReady(player1, new SeanceBoard());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(board.getCounterCount(CounterType.SOUL)).isEqualTo(1);
    }

    @Test
    void activatedAbilityAddsRestrictedManaAndPaysForInstant() {
        Permanent board = addCreatureReady(player1, new SeanceBoard());
        board.setCounterCount(CounterType.SOUL, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        Set<CardSubtype> allowedSubtypes = Set.of(CardSubtype.DEMON, CardSubtype.SPIRIT);
        assertThat(pool.getInstantSorceryOrSubtypeSpellOnlyManaForColor(allowedSubtypes, ManaColor.RED))
                .isEqualTo(2);

        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(pool.getInstantSorceryOrSubtypeSpellOnlyManaForColor(allowedSubtypes, ManaColor.RED))
                .isEqualTo(1);
    }

    @Test
    void restrictedManaCannotPayForOtherSpells() {
        Permanent board = addCreatureReady(player1, new SeanceBoard());
        board.setCounterCount(CounterType.SOUL, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.setHand(player1, List.of(new PropheticPrism()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
