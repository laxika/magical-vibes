package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.cards.l.LeatherArmor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuardianOfFaith.class, GrizzlyBears.class, HillGiantHerdgorger.class, LeatherArmor.class})
class GuardianOfFaithTest extends BaseCardTest {

    @Test
    @DisplayName("Phases out any number of other creatures you control")
    void phasesOutSelectedCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GuardianOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard() instanceof GuardianOfFaith);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(first, second);
    }

    @Test
    @DisplayName("Can choose no creatures")
    void canChooseNoCreatures() {
        harness.setHand(player1, List.of(new GuardianOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).isNullOrEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard() instanceof GuardianOfFaith);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GuardianOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
    }

    @Test
    @DisplayName("Phased-out creatures phase in during their controller's next untap")
    void phasesBackInDuringNextUntap() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GuardianOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    void canFlashInDuringOpponentsTurnAndProtectAnotherGuardian() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GuardianOfFaith());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GuardianOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature).hasSize(2);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).isEmpty();
    }

    @Test
    void canPhaseOutMoreThanNinetyNineCreatures() {
        List<Permanent> creatures = java.util.stream.IntStream.range(0, 100)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new GuardianOfFaith()))
                .toList();
        harness.setHand(player1, List.of(new GuardianOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0, creatures.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).containsAll(creatures);
    }

    @Test
    void phasesOutAttachmentsAndPreservesCountersWithoutRetriggeringEnters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeatherArmor());
        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creature.tap();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new GuardianOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature, equipment);
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, equipment);
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        harness.assertLife(player1, 20);
    }
}
