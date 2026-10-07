package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.k.KyoshiWarriorGuard;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheArtOfTea.class, KyoshiWarriorGuard.class})
class TheArtOfTeaTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on a controlled creature and creates a Food")
    void putsCounterAndCreatesFood() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KyoshiWarriorGuard());
        castTea(creature.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    @DisplayName("Creates a Food when no creature is chosen")
    void createsFoodWithoutTarget() {
        castTea();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new KyoshiWarriorGuard());
        harness.setHand(player1, List.of(new TheArtOfTea()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can choose no target even when a controlled creature is available")
    void canDeclineAvailableTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KyoshiWarriorGuard());

        castTea();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates no Food if the chosen creature leaves before resolution")
    void createsNoFoodWhenTargetLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KyoshiWarriorGuard());
        harness.setHand(player1, List.of(new TheArtOfTea()));
        addMana();
        harness.castInstant(player1, 0, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.setGraveyard(player1, List.of(creature.getCard()));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof TheArtOfTea);
    }

    @Test
    @DisplayName("Creates no Food if the chosen creature changes controller")
    void createsNoFoodWhenTargetChangesController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new KyoshiWarriorGuard());
        harness.setHand(player1, List.of(new TheArtOfTea()));
        addMana();
        harness.castInstant(player1, 0, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(countPermanents(player2, "Food")).isZero();
    }

    @Test
    @DisplayName("Food is sacrificed as a cost and grants life only on resolution")
    void foodSacrificeUsesTheStack() {
        castTea();
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
        assertThat(gd.getLife(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Food cannot be activated without two mana")
    void foodRequiresMana() {
        castTea();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Tapped Food cannot be activated")
    void foodRequiresUntappedToken() {
        castTea();
        findPermanent(player1, "Food").tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a controlled noncreature Food")
    void cannotTargetFood() {
        castTea();
        harness.setHand(player1, List.of(new TheArtOfTea()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, findPermanent(player1, "Food").getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    private void castTea() {
        harness.setHand(player1, List.of(new TheArtOfTea()));
        addMana();
        harness.castAndResolveInstant(player1, 0);
    }

    private void castTea(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new TheArtOfTea()));
        addMana();
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
