package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HyraxTowerScout.class, NyxbornColossus.class, Island.class})
class HyraxTowerScoutTest extends BaseCardTest {

    @Test
    void entersAndUntapsTargetCreatureYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new NyxbornColossus());
        bears.tap();
        castScoutTargeting(bears);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    void entersAndUntapsTargetCreatureAnOpponentControls() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        bears.tap();
        castScoutTargeting(bears);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    void cannotTargetANoncreaturePermanent() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.tap();
        harness.setHand(player1, List.of(new HyraxTowerScout()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetItselfWhenEnteringWithoutBeingCast() {
        Permanent scout = harness.enterBattlefieldAndReturn(player1, new HyraxTowerScout());
        harness.handlePermanentChosen(player1, scout.getId());
        scout.tap();

        harness.passBothPriorities();

        assertThat(scout.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetAnUntappedCreatureWithoutUntappingOtherCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NyxbornColossus());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        other.tap();

        castScoutTargeting(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotUntapAnotherCreatureWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        target.tap();
        other.tap();
        Permanent scout = harness.enterBattlefieldAndReturn(player1, new HyraxTowerScout());
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(other.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(scout);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerStillUntapsTargetAfterScoutLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        target.tap();
        Permanent scout = harness.enterBattlefieldAndReturn(player1, new HyraxTowerScout());
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(scout);
        gd.playerGraveyards.get(player1.getId()).add(scout.getCard());

        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void castScoutTargeting(Permanent target) {
        harness.setHand(player1, List.of(new HyraxTowerScout()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
