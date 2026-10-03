package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DireWolfProwler;
import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlackDragon.class, DireWolfProwler.class, HillGiantHerdgorger.class})
class BlackDragonTest extends BaseCardTest {

    @Test
    void etbGivesTargetOpponentCreatureMinusThreeMinusThree() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());

        castBlackDragon(giant.getId());

        assertThat(giant.getPowerModifier()).isEqualTo(-3);
        assertThat(giant.getToughnessModifier()).isEqualTo(-3);
    }

    @Test
    void etbMinusThreeMinusThreeKillsSmallCreature() {
        harness.addToBattlefield(player2, new DireWolfProwler());

        UUID targetId = harness.getPermanentId(player2, "Dire Wolf Prowler");
        castBlackDragon(targetId);

        harness.assertNotOnBattlefield(player2, "Dire Wolf Prowler");
    }

    @Test
    void etbCannotTargetCreatureItsControllerControls() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());
        harness.setHand(player1, List.of(new BlackDragon()));
        addBlackDragonMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canEnterWithoutAnOpponentCreatureToTarget() {
        harness.castFromHand(player1, new BlackDragon(), "{5}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Black Dragon");
    }

    @Test
    void penaltyExpiresAfterEndOfTurn() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());

        castBlackDragon(giant.getId());
        harness.passUntil(TurnStep.END_STEP);

        assertThat(giant.getPowerModifier()).isEqualTo(-3);
        assertThat(giant.getToughnessModifier()).isEqualTo(-3);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(giant.getPowerModifier()).isZero();
        assertThat(giant.getToughnessModifier()).isZero();
    }

    @Test
    void onlyChosenCreatureGetsThePenalty() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());

        castBlackDragon(target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(target.getToughnessModifier()).isEqualTo(-3);
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player2, "Dire Wolf Prowler");
    }

    @Test
    void triggerResolvesAfterSourceLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        harness.setHand(player1, List.of(new BlackDragon()));
        addBlackDragonMana();
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        UUID dragonId = harness.getPermanentId(player1, "Black Dragon");
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(dragonId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Black Dragon");
        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(target.getToughnessModifier()).isEqualTo(-3);
    }

    private void castBlackDragon(UUID targetId) {
        harness.setHand(player1, List.of(new BlackDragon()));
        addBlackDragonMana();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addBlackDragonMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
