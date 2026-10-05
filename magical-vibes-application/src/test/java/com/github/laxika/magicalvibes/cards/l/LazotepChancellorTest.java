package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.StepThrough;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LazotepChancellor.class, Censor.class, GrizzlyBears.class, StepThrough.class})
class LazotepChancellorTest extends BaseCardTest {

    @Test
    void payingForDiscardTriggerCreatesAndAmassesZombieArmy() {
        harness.addToBattlefield(player1, new LazotepChancellor());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        Permanent army = findPermanent(player1, "Zombie Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE, CardSubtype.ARMY);
    }

    @Test
    void decliningDiscardTriggerDoesNotAmass() {
        harness.addToBattlefield(player1, new LazotepChancellor());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Zombie Army")).isEmpty();
    }

    @Test
    void payingForDiscardTriggerAddsCountersToExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        harness.addToBattlefield(player1, new LazotepChancellor());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Zombie Army")).isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ARMY, CardSubtype.ZOMBIE);
    }

    @Test
    @CardUsed({LazotepChancellor.class, StepThrough.class})
    void cannotAmassWithoutPayingForDiscardTrigger() {
        harness.addToBattlefield(player1, new LazotepChancellor());
        harness.setHand(player1, List.of(new StepThrough()));
        harness.setLibrary(player1, List.of(new LazotepChancellor()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Zombie Army")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        assertThat(findPermanents(player1, "Zombie Army")).isEmpty();
    }

    @Test
    @CardUsed({LazotepChancellor.class, StepThrough.class})
    void opponentDiscardDoesNotTriggerChancellor() {
        harness.addToBattlefield(player1, new LazotepChancellor());
        harness.setHand(player2, List.of(new StepThrough()));
        harness.setLibrary(player2, List.of(new LazotepChancellor()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player2, 0);
        assertThat(findPermanents(player1, "Zombie Army")).isEmpty();
        assertThat(findPermanents(player2, "Zombie Army")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @CardUsed({LazotepChancellor.class, StepThrough.class})
    void eachChancellorRequiresItsOwnPaymentForOneDiscard() {
        harness.addToBattlefield(player1, new LazotepChancellor());
        harness.addToBattlefield(player1, new LazotepChancellor());
        harness.setHand(player1, List.of(new StepThrough()));
        harness.setLibrary(player1, List.of(new LazotepChancellor()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Zombie Army")).hasSize(1);
        assertThat(findPermanent(player1, "Zombie Army").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.handleCardChosen(player1, 0);
    }
}
