package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.u.UrzasBauble;
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

@CardUsed({Spaceshift.class, GrizzlyBears.class, UrzasBauble.class, Island.class})
class SpaceshiftTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles and returns a target creature with a +1/+1 counter")
    void flickersCreatureWithCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        giveSpaceshift();

        harness.castAndResolveInstant(player1, 0, target.getId());

        Permanent returned = findPermanent(player2, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiles and returns a target artifact with a +1/+1 counter")
    void flickersArtifactWithCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new UrzasBauble());
        giveSpaceshift();

        harness.castAndResolveInstant(player1, 0, target.getId());

        Permanent returned = findPermanent(player1, "Urza's Bauble");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());
        giveSpaceshift();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    @DisplayName("Returns a stolen creature to its owner rather than its controller")
    void returnsStolenCreatureToOwner() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(target.getId(), player2.getId());
        giveSpaceshift();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        Permanent returned = findPermanent(player2, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns a fresh untapped creature with only the new counter")
    void resetsOldPermanentState() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        target.tap();
        target.setSummoningSick(false);
        giveSpaceshift();

        harness.castAndResolveInstant(player1, 0, target.getId());

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Does not return a target that leaves the battlefield before resolution")
    void doesNotReturnMissingTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        giveSpaceshift();
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private void giveSpaceshift() {
        harness.setHand(player1, List.of(new Spaceshift()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
