package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.ConcordiaPegasus;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JusticiarsPortal.class, ConcordiaPegasus.class})
class JusticiarsPortalTest extends BaseCardTest {

    @Test
    @DisplayName("Flickers a creature you control and grants it first strike until end of turn")
    void flickersAndGrantsFirstStrike() {
        harness.addToBattlefield(player1, new ConcordiaPegasus());
        harness.setHand(player1, List.of(new JusticiarsPortal()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID creatureId = harness.getPermanentId(player1, "Concordia Pegasus");
        harness.castAndResolveInstant(player1, 0, creatureId);

        Permanent returned = findPermanent(player1, "Concordia Pegasus");
        assertThat(returned.getId()).isNotEqualTo(creatureId);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike wears off at cleanup")
    void firstStrikeWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new ConcordiaPegasus());
        harness.setHand(player1, List.of(new JusticiarsPortal()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID creatureId = harness.getPermanentId(player1, "Concordia Pegasus");
        harness.castAndResolveInstant(player1, 0, creatureId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Concordia Pegasus");
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new ConcordiaPegasus());
        harness.setHand(player1, List.of(new JusticiarsPortal()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID creatureId = harness.getPermanentId(player2, "Concordia Pegasus");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A borrowed creature returns to its owner and still gains first strike")
    void borrowedCreatureReturnsToOwner() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new ConcordiaPegasus());
        gd.stolenCreatures.put(original.getId(), player2.getId());
        harness.setHand(player1, List.of(new JusticiarsPortal()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, original.getId());

        harness.assertNotOnBattlefield(player1, "Concordia Pegasus");
        Permanent returned = findPermanent(player2, "Concordia Pegasus");
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The returned creature is untapped and loses its old counters and damage")
    void returnedCreatureHasFreshState() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new ConcordiaPegasus());
        original.tap();
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        original.setMarkedDamage(1);
        harness.setHand(player1, List.of(new JusticiarsPortal()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, original.getId());

        Permanent returned = findPermanent(player1, "Concordia Pegasus");
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(returned.getMarkedDamage()).isZero();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A creature that leaves before resolution is not returned from the graveyard")
    void absentTargetDoesNotReturn() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new ConcordiaPegasus());
        harness.setHand(player1, List.of(new JusticiarsPortal()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, original.getId());
        gd.playerBattlefields.get(player1.getId()).remove(original);
        gd.playerGraveyards.get(player1.getId()).add(original.getCard());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Concordia Pegasus");
        harness.assertInGraveyard(player1, "Concordia Pegasus");
        harness.assertInGraveyard(player1, "Justiciar's Portal");
    }
}
