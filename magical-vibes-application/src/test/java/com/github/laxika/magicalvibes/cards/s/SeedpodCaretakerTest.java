package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.ProgenitorExarch;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeedpodCaretaker.class, Ornithopter.class, ProgenitorExarch.class})
class SeedpodCaretakerTest extends BaseCardTest {

    @Test
    void putsACounterOnAnArtifactOrCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        castCaretaker();
        harness.handleListChoice(player1, "Put a +1/+1 counter on target artifact or creature you control.");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void transformsAnIncubatorTokenYouControl() {
        createIncubator();
        Permanent incubator = findPermanent(player1, "Incubator");

        castCaretaker();
        harness.handleListChoice(player1, "Transform target Incubator token you control.");
        harness.handlePermanentChosen(player1, incubator.getId());
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
    }

    @Test
    void rejectsAnOpponentPermanentForTheCounterMode() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castCaretaker();
        harness.handleListChoice(player1, "Put a +1/+1 counter on target artifact or creature you control.");
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Seedpod Caretaker").getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castCaretaker() {
        harness.setHand(player1, List.of(new SeedpodCaretaker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void createIncubator() {
        harness.setHand(player1, List.of(new ProgenitorExarch()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
