package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({ExperimentalLabStaffRoom.class, Forest.class, GrizzlyBears.class})
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
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        assertThat(attacker.isFaceDown()).isTrue();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Turn that creature face up");

        assertThat(attacker.isFaceDown()).isFalse();
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
