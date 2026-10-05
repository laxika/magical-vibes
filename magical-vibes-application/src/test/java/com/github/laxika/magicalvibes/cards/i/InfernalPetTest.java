package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DemonicGifts;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfernalPet.class, DemonicGifts.class})
class InfernalPetTest extends BaseCardTest {

    @Test
    @DisplayName("Your second spell puts a +1/+1 counter on Infernal Pet and grants flying")
    void secondSpellPutsCounterAndGrantsFlying() {
        Permanent pet = addCreatureReady(player1, new InfernalPet());

        harness.setHand(player1, List.of(new DemonicGifts(), new DemonicGifts(), new DemonicGifts()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveInstant(player1, 0, pet.getId());
        assertThat(pet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(pet.getGrantedKeywords()).doesNotContain(Keyword.FLYING);

        harness.castAndResolveInstant(player1, 0, pet.getId());

        assertThat(pet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(pet.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.castAndResolveInstant(player1, 0, pet.getId());
        assertThat(pet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flying granted by Infernal Pet wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent pet = addCreatureReady(player1, new InfernalPet());

        harness.setHand(player1, List.of(new DemonicGifts(), new DemonicGifts()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, pet.getId());
        harness.castAndResolveInstant(player1, 0, pet.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(pet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(pet.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Opponent spells neither trigger Infernal Pet nor count toward your second spell")
    void opponentSpellsDoNotCount() {
        Permanent pet = addCreatureReady(player1, new InfernalPet());
        harness.setHand(player2, List.of(new DemonicGifts(), new DemonicGifts()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player2, 0, pet.getId());
        harness.castAndResolveInstant(player2, 0, pet.getId());
        assertThat(pet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(pet.getGrantedKeywords()).doesNotContain(Keyword.FLYING);

        harness.setHand(player1, List.of(new DemonicGifts(), new DemonicGifts()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player1, 0, pet.getId());
        assertThat(pet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.castAndResolveInstant(player1, 0, pet.getId());
        assertThat(pet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(pet.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("The spell count resets and Infernal Pet can trigger again during an opponent's turn")
    void triggersAgainOnOpponentsTurn() {
        Permanent pet = addCreatureReady(player1, new InfernalPet());
        harness.setHand(player1, List.of(new DemonicGifts(), new DemonicGifts()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player1, 0, pet.getId());
        harness.castAndResolveInstant(player1, 0, pet.getId());
        assertThat(pet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new DemonicGifts(), new DemonicGifts()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, pet.getId());
        assertThat(pet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(pet.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
        harness.castAndResolveInstant(player1, 0, pet.getId());
        assertThat(pet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(pet.getGrantedKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Casting Infernal Pet as your first spell counts toward its second-spell trigger")
    void countsItsOwnCastBeforeEnteringBattlefield() {
        harness.setHand(player1, List.of(new InfernalPet(), new DemonicGifts()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent pet = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(pet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castAndResolveInstant(player1, 0, pet.getId());
        assertThat(pet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(pet.getGrantedKeywords()).contains(Keyword.FLYING);
    }
}
