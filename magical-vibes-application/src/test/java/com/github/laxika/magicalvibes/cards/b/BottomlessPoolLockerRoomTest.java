package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FearOfLostTeeth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ManaColor.BLUE;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BottomlessPoolLockerRoom.class, FearOfLostTeeth.class})
class BottomlessPoolLockerRoomTest extends BaseCardTest {

    @Test
    void bottomlessPoolReturnsTheChosenCreatureToItsOwnersHand() {
        Card creatureCard = creature("Target creature");
        Permanent creature = addCreatureReady(player2, creatureCard);

        castRoom(0);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerHands.get(player2.getId())).contains(creatureCard);
    }

    @Test
    void bottomlessPoolCanBeResolvedWithoutChoosingACreature() {
        Permanent creature = addCreatureReady(player2, creature("Creature"));

        castRoom(0);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void lockerRoomDrawsOnlyOnceForMultipleCreaturesDealingCombatDamage() {
        castRoom(1);
        Permanent firstAttacker = addCreatureReady(player1, creature("First attacker"));
        Permanent secondAttacker = addCreatureReady(player1, creature("Second attacker"));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker)));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void unlockingBottomlessPoolAfterCastingLockerRoomReturnsACreature() {
        Permanent room = castRoom(1);
        Card targetCard = new FearOfLostTeeth();
        Permanent target = addCreatureReady(player2, targetCard);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, BLUE, 1);
        harness.unlockRoomDoor(player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player2.getId())).contains(targetCard);
    }

    @Test
    void castingLockerRoomDoesNotTriggerBottomlessPool() {
        Permanent target = addCreatureReady(player2, new FearOfLostTeeth());

        castRoom(1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(target.getCard());
    }

    @Test
    void lockedLockerRoomDoesNotDrawForCombatDamage() {
        castRoom(0);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        Permanent attacker = addCreatureReady(player1, new FearOfLostTeeth());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    void unlockingLockerRoomEnablesItsCombatDamageAbilityWithoutBouncingACreature() {
        Permanent room = castRoom(0);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        Permanent attacker = addCreatureReady(player1, new FearOfLostTeeth());
        harness.setLibrary(player1, List.of(new FearOfLostTeeth()));

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, BLUE, 5);
        harness.unlockRoomDoor(player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void lockerRoomDoesNotDrawWhenAnOpponentsCreatureDealsCombatDamage() {
        castRoom(1);
        Permanent attacker = addCreatureReady(player2, new FearOfLostTeeth());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
        resolveCombat(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    void bottomlessPoolCanReturnItsControllersCreature() {
        Card targetCard = new FearOfLostTeeth();
        Permanent target = addCreatureReady(player1, targetCard);

        castRoom(0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player1.getId())).contains(targetCard);
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new BottomlessPoolLockerRoom()));
        harness.addMana(player1, BLUE, doorIndex == 0 ? 1 : 5);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ROOM))
                .findFirst()
                .orElseThrow();
    }

    private Card creature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(1);
        card.setToughness(1);
        return card;
    }
}
