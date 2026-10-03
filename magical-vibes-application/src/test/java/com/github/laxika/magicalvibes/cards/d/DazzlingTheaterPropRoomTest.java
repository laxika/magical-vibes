package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CultHealer;
import com.github.laxika.magicalvibes.cards.g.Glimmerlight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ManaColor.WHITE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DazzlingTheaterPropRoom.class, CultHealer.class, Glimmerlight.class})
class DazzlingTheaterPropRoomTest extends BaseCardTest {

    @Test
    void castingDazzlingTheaterUnlocksItsDoorAndGrantsConvokeToCreatureSpells() {
        Permanent room = castRoom(0);
        assertThat(room.isRoomDoorUnlocked(0)).isTrue();
        assertThat(room.isRoomDoorUnlocked(1)).isFalse();

        Permanent convoker = addCreatureReady(player1, simpleCreature("Convoker"));
        Card creatureSpell = simpleCreature("Creature spell");
        harness.setHand(player1, List.of(creatureSpell));
        harness.addMana(player1, WHITE, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(convoker.getId()));

        assertThat(convoker.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Creature spell");
    }

    @Test
    void castingPropRoomUnlocksItsDoorAndUntapsCreaturesDuringOpponentsUntapStep() {
        Permanent room = castRoom(1);
        assertThat(room.isRoomDoorUnlocked(1)).isTrue();
        assertThat(room.isRoomDoorUnlocked(0)).isFalse();

        Permanent creature = addCreatureReady(player1, simpleCreature("Creature"));
        creature.tap();

        harness.forceStep(TurnStep.UNTAP);
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void fullyUnlockingRoomTriggersAlliedRoomAbilities() {
        Permanent room = castRoom(0);
        Permanent healer = harness.addToBattlefieldAndReturn(player1, new CultHealer());
        harness.addMana(player1, WHITE, 3);

        harness.unlockRoomDoor(player1, 0, 1);
        assertThat(room.isRoomFullyUnlocked()).isTrue();
        assertThat(gd.stack).isNotEmpty();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, healer,
                com.github.laxika.magicalvibes.model.Keyword.LIFELINK)).isTrue();
    }

    @Test
    void lockedTheaterDoesNotGrantConvoke() {
        castRoom(1);
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new CultHealer());
        harness.setHand(player1, List.of(new CultHealer()));
        harness.addMana(player1, WHITE, 2);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(
                player1, 0, List.of(), List.of(convoker.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(convoker.isTapped()).isFalse();
        harness.assertInHand(player1, "Cult Healer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unlockingTheaterAllowsSummoningSickCreaturesToPayTheEntireCreatureCost() {
        castRoom(1);
        harness.addMana(player1, WHITE, 4);
        harness.unlockRoomDoor(player1, 0, 0);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CultHealer());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CultHealer());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new CultHealer());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        third.setSummoningSick(true);
        harness.setHand(player1, List.of(new CultHealer()));

        harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(first.getId(), second.getId(), third.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Cult Healer")).isEqualTo(4);
        harness.assertNotInHand(player1, "Cult Healer");
    }

    @Test
    void theaterDoesNotGrantConvokeToOpponentsCreatureSpells() {
        castRoom(0);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent convoker = harness.addToBattlefieldAndReturn(player2, new CultHealer());
        harness.setHand(player2, List.of(new CultHealer()));
        harness.addMana(player2, WHITE, 2);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(
                player2, 0, List.of(), List.of(convoker.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(convoker.isTapped()).isFalse();
        harness.assertInHand(player2, "Cult Healer");
    }

    @Test
    void theaterDoesNotGrantConvokeToNoncreatureSpells() {
        castRoom(0);
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new CultHealer());
        harness.setHand(player1, List.of(new Glimmerlight()));
        harness.addMana(player1, WHITE, 1);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(
                player1, 0, List.of(), List.of(convoker.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(convoker.isTapped()).isFalse();
        harness.assertInHand(player1, "Glimmerlight");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void lockedPropRoomDoesNotUntapCreaturesDuringOpponentsUntapStep() {
        castRoom(0);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CultHealer());
        creature.tap();

        harness.forceStep(TurnStep.UNTAP);
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void unlockingPropRoomUntapsAllCreaturesButLeavesNoncreaturesTapped() {
        Permanent room = castRoom(0);
        harness.addMana(player1, WHITE, 3);
        harness.unlockRoomDoor(player1, 0, 1);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CultHealer());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CultHealer());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        first.tap();
        second.tap();
        room.tap();

        harness.forceStep(TurnStep.UNTAP);
        harness.performUntapStep(player2);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(room.isTapped()).isTrue();
        assertThat(first.isSummoningSick()).isTrue();
        assertThat(second.isSummoningSick()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void propRoomDoesNotUntapOpponentsCreaturesDuringItsControllersUntapStep() {
        castRoom(1);
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new CultHealer());
        opposingCreature.tap();

        harness.forceStep(TurnStep.UNTAP);
        harness.performUntapStep(player1);

        assertThat(opposingCreature.isTapped()).isTrue();
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, WHITE, doorIndex == 0 ? 4 : 3);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private Card simpleCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}{W}");
        card.setPower(1);
        card.setToughness(1);
        return card;
    }
}
