package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.InnocuousRat;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ManaColor.WHITE;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrandEntrywayElegantRotunda.class, InnocuousRat.class})
class GrandEntrywayElegantRotundaTest extends BaseCardTest {

    @Test
    void unlockingGrandEntrywayCreatesAGlimmerEnchantmentCreatureToken() {
        Permanent room = castRoom(0);

        assertThat(room.isRoomDoorUnlocked(0)).isTrue();
        assertThat(room.isRoomDoorUnlocked(1)).isFalse();
        Permanent glimmer = findPermanent(player1, "Glimmer");
        assertThat(glimmer.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(glimmer.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
        assertThat(glimmer.getCard().getSubtypes()).containsExactly(CardSubtype.GLIMMER);
    }

    @Test
    void unlockingElegantRotundaPutsCountersOnUpToTwoTargetCreatures() {
        Permanent room = castRoom(0);
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new InnocuousRat());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new InnocuousRat());
        harness.addMana(player1, WHITE, 3);

        harness.unlockRoomDoor(player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, firstCreature.getId());
        harness.handlePermanentChosen(player1, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new GrandEntrywayElegantRotunda()));
        harness.addMana(player1, WHITE, doorIndex == 0 ? 2 : 3);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();
        return findPermanent(player1, "Grand Entryway // Elegant Rotunda");
    }

    @Test
    void castingElegantRotundaCanTargetAnOpponentsCreatureWithoutCreatingAGlimmer() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new InnocuousRat());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new InnocuousRat());

        Permanent room = castRoom(1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(room.isRoomDoorUnlocked(0)).isFalse();
        assertThat(room.isRoomDoorUnlocked(1)).isTrue();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Glimmer")).isZero();
    }

    @Test
    void elegantRotundaCanChooseZeroTargetsEvenWhenCreaturesAreAvailable() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new InnocuousRat());

        castRoom(1);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Glimmer")).isZero();
    }

    @Test
    void elegantRotundaCanResolveWithoutAnyCreatures() {
        Permanent room = castRoom(1);

        assertThat(room.isRoomDoorUnlocked(1)).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Glimmer")).isZero();
    }

    @Test
    void unlockingGrandEntrywayAfterCastingElegantRotundaCreatesExactlyOneGlimmer() {
        Permanent room = castRoom(1);
        harness.addMana(player1, WHITE, 2);

        harness.unlockRoomDoor(player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 0);
        resolveAllTriggers();

        assertThat(room.isRoomFullyUnlocked()).isTrue();
        assertThat(countPermanents(player1, "Glimmer")).isEqualTo(1);
        Permanent glimmer = findPermanent(player1, "Glimmer");
        assertThat(gqs.getEffectivePower(gd, glimmer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, glimmer)).isEqualTo(1);
        assertThat(glimmer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void elegantRotundaStillCountersTheRemainingTargetWhenOneTargetLeaves() {
        Permanent room = castRoom(0);
        Permanent glimmer = findPermanent(player1, "Glimmer");
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new InnocuousRat());
        harness.addMana(player1, WHITE, 3);

        harness.unlockRoomDoor(player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, glimmer.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(glimmer);
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Glimmer")).isZero();
    }
}
