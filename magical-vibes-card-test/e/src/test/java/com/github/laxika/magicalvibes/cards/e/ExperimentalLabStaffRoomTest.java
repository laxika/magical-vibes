package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.Cultivate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExperimentalLabStaffRoom.class, Forest.class, GrizzlyBears.class,
        Cultivate.class, SakuraTribeElder.class})
class ExperimentalLabStaffRoomTest extends BaseCardTest {

    @Test
    void unlockingExperimentalLabManifestsDreadAndPutsTwoPlusOneCountersAndATrampleCounterOnThatCreature() {
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setHand(player1, List.of(new ExperimentalLabStaffRoom()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(manifestedCard, graveyardCard);

        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested)
                .findFirst()
                .orElseThrow();
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(manifested.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, manifested, com.github.laxika.magicalvibes.model.Keyword.TRAMPLE))
                .isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
    }

    @Test
    void staffRoomPutsACounterOnTheCreatureThatDealtCombatDamage() {
        castRoom(1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put a +1/+1 counter on it");

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void staffRoomTurnsTheCombatDamageDealerFaceUp() {
        castRoom(1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        assertThat(attacker.isFaceDown()).isTrue();
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Turn that creature face up");

        assertThat(attacker.isFaceDown()).isFalse();
    }

    @Test
    void lockedStaffRoomDoesNotTriggerWhenACreatureDealsCombatDamage() {
        harness.addToBattlefield(player1, new ExperimentalLabStaffRoom());
        Permanent attacker = addCreatureReady(player1, new SakuraTribeElder());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        resolveCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void staffRoomCannotTurnAManifestedSorceryFaceUp() {
        castRoom(1);
        Permanent attacker = addCreatureReady(player1, new Cultivate());
        attacker.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        attacker.setManifested(true);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Turn that creature face up");

        assertThat(attacker.isFaceDown()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void staffRoomCanTurnAManifestedLandFaceUp() {
        castRoom(1);
        Permanent attacker = addCreatureReady(player1, new Forest());
        attacker.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        attacker.setManifested(true);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Turn that creature face up");

        assertThat(attacker.isFaceDown()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    void experimentalLabManifestsTheOnlyCardInTheLibraryAndStillAddsItsCounters() {
        Card card = new SakuraTribeElder();
        harness.setLibrary(player1, List.of(card));
        castRoom(0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested)
                .findFirst().orElseThrow();
        assertThat(manifested.getOriginalCard()).isSameAs(card);
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(manifested.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void experimentalLabWithAnEmptyLibraryDoesNotPutCountersOnAnotherCreature() {
        Permanent creature = addCreatureReady(player1, new SakuraTribeElder());
        harness.setLibrary(player1, List.of());
        castRoom(0);

        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.getCounterCount(CounterType.TRAMPLE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new ExperimentalLabStaffRoom()));
        harness.addMana(player1, ManaColor.GREEN, doorIndex == 0 ? 4 : 3);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes()
                        .contains(com.github.laxika.magicalvibes.model.CardSubtype.ROOM))
                .findFirst()
                .orElseThrow();
    }
}
