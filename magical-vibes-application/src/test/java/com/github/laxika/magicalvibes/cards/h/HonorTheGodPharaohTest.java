package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HonorTheGodPharaoh.class, PrimordialWurm.class})
class HonorTheGodPharaohTest extends BaseCardTest {

    @Test
    void discardsDrawsTwoAndAmassesWithoutAnArmy() {
        harness.setHand(player1, List.of(new HonorTheGodPharaoh(), new PrimordialWurm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        Permanent army = findPermanent(player1, "Zombie Army");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(army.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.ZOMBIE, CardSubtype.ARMY);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void discardsDrawsTwoAndAmassesOnAnExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        harness.setHand(player1, List.of(new HonorTheGodPharaoh(), new PrimordialWurm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
    }

    @Test
    void cannotBeCastWithoutAnotherCardToDiscard() {
        harness.setHand(player1, List.of(new HonorTheGodPharaoh()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithDiscard(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void discardIsPaidBeforeDrawingOrAmassing() {
        PrimordialWurm discarded = new PrimordialWurm();
        harness.setHand(player1, List.of(new HonorTheGodPharaoh(), discarded));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithDiscard(player1, 0, 1);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void opposingArmyDoesNotPreventCreatingOwnArmy() {
        Permanent opposingArmy = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        opposingArmy.getGrantedSubtypes().add(CardSubtype.ARMY);
        harness.setHand(player1, List.of(new HonorTheGodPharaoh(), new HonorTheGodPharaoh()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(army -> assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
        assertThat(opposingArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingArmy.getGrantedSubtypes()).doesNotContain(CardSubtype.ZOMBIE);
    }

    @Test
    void choosesOnlyOneOfMultipleArmiesAndGrantsItZombie() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        first.getGrantedSubtypes().add(CardSubtype.ARMY);
        second.getGrantedSubtypes().add(CardSubtype.ARMY);
        harness.setHand(player1, List.of(new HonorTheGodPharaoh(), new HonorTheGodPharaoh()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(first.getGrantedSubtypes()).doesNotContain(CardSubtype.ZOMBIE);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getGrantedSubtypes()).contains(CardSubtype.ARMY, CardSubtype.ZOMBIE);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void cannotDeclineChoosingAnArmyWhenMultipleArmiesExist() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        first.getGrantedSubtypes().add(CardSubtype.ARMY);
        second.getGrantedSubtypes().add(CardSubtype.ARMY);
        harness.setHand(player1, List.of(new HonorTheGodPharaoh(), new HonorTheGodPharaoh()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({HonorTheGodPharaoh.class, DoublingSeason.class})
    void doubledArmyCreationPutsCountersOnOnlyOneChosenArmy() {
        harness.addToBattlefield(player1, new DoublingSeason());
        harness.setHand(player1, List.of(new HonorTheGodPharaoh(), new HonorTheGodPharaoh()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        List<Permanent> armies = findPermanents(player1, "Zombie Army");
        assertThat(armies).hasSize(2);
        harness.handleMultiplePermanentsChosen(player1, List.of(armies.getFirst().getId()));

        assertThat(armies.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .containsExactly(armies.getFirst());
    }
}
