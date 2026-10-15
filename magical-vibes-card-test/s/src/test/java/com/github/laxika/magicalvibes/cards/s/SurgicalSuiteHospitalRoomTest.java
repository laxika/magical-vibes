package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.cards.l.LivingPhone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.laxika.magicalvibes.model.ManaColor.WHITE;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SurgicalSuiteHospitalRoom.class, LivingPhone.class})
class SurgicalSuiteHospitalRoomTest extends BaseCardTest {

    @Test
    void surgicalSuiteReturnsTargetCreatureWithManaValueThreeOrLess() {
        Card eligible = creature("Eligible creature", "{3}");
        Card tooExpensive = creature("Too expensive", "{4}");
        harness.setGraveyard(player1, List.of(eligible, tooExpensive));

        castRoom(0);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);

        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(eligible.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(tooExpensive);
    }

    @Test
    void hospitalRoomPutsOneCounterOnOneAttackingCreatureEachCombat() {
        castRoom(1);
        Permanent firstAttacker = addCreatureReady(player1, creature("First attacker", "{1}"));
        Permanent secondAttacker = addCreatureReady(player1, creature("Second attacker", "{1}"));

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker)));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstAttacker.getId(), secondAttacker.getId());

        harness.handlePermanentChosen(player1, firstAttacker.getId());
        harness.passBothPriorities();

        assertThat(firstAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void hospitalRoomDoesNotReturnCreaturesUntilSurgicalSuiteIsUnlocked() {
        Card creature = new LivingPhone();
        harness.setGraveyard(player1, List.of(creature));
        Permanent room = castRoom(1);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, WHITE, 2);
        harness.unlockRoomDoor(player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Living Phone");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void surgicalSuiteCannotReturnOpponentCreatureOrNoncreatureCard() {
        Card opponentCreature = new LivingPhone();
        Card noncreature = new SurgicalSuiteHospitalRoom();
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setGraveyard(player1, List.of(noncreature));

        castRoom(0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCreature);
        harness.assertNotOnBattlefield(player1, "Living Phone");
    }

    @Test
    void surgicalSuiteDoesNotReturnTargetRemovedFromGraveyardBeforeResolution() {
        Card creature = new LivingPhone();
        harness.setGraveyard(player1, List.of(creature));
        castRoom(0);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(creature);
        harness.setHand(player1, List.of(creature));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Living Phone");
        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
    }

    @Test
    void lockedHospitalRoomDoesNotTriggerWhenYouAttack() {
        castRoom(0);
        Permanent attacker = addCreatureReady(player1, new LivingPhone());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void hospitalRoomExcludesNonattackingCreaturesAndRechecksTargetAtResolution() {
        castRoom(1);
        Permanent first = addCreatureReady(player1, new LivingPhone());
        Permanent second = addCreatureReady(player1, new LivingPhone());
        Permanent nonattacker = addCreatureReady(player1, new LivingPhone());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(first),
                gd.playerBattlefields.get(player1.getId()).indexOf(second)));
        harness.passBothPriorities();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handlePermanentChosen(player1, first.getId());
        first.setAttacking(false);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(nonattacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void unlockingHospitalRoomEnablesItsAttackAbility() {
        Permanent room = castRoom(0);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, WHITE, 4);
        harness.unlockRoomDoor(player1, gd.playerBattlefields.get(player1.getId()).indexOf(room), 1);
        Permanent first = addCreatureReady(player1, new LivingPhone());
        Permanent second = addCreatureReady(player1, new LivingPhone());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(first),
                gd.playerBattlefields.get(player1.getId()).indexOf(second)));
        harness.passBothPriorities();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void hospitalRoomDoesNotTriggerWhenOpponentAttacks() {
        castRoom(1);
        Permanent attacker = addCreatureReady(player2, new LivingPhone());

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new SurgicalSuiteHospitalRoom()));
        harness.addMana(player1, WHITE, doorIndex == 0 ? 2 : 4);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ROOM))
                .findFirst()
                .orElseThrow();
    }
    private Card creature(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        return card;
    }
}
