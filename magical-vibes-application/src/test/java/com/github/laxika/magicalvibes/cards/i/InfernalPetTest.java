package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InfernalPetTest extends BaseCardTest {

    @Test
    @DisplayName("Your second spell puts a +1/+1 counter on Infernal Pet and grants flying")
    void secondSpellPutsCounterAndGrantsFlying() {
        Permanent pet = addCreatureReady(player1, new InfernalPet());

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(pet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(pet.getGrantedKeywords()).doesNotContain(Keyword.FLYING);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(pet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(pet.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(pet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flying granted by Infernal Pet wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent pet = addCreatureReady(player1, new InfernalPet());

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(pet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(pet.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }
}
