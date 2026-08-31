package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SqueakBy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CheekyHouseMouse.class, SqueakBy.class, GrizzlyBears.class, HillGiant.class})
class CheekyHouseMouseTest extends BaseCardTest {

    @Test
    void adventureBoostsTargetCreatureAndExilesCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        CheekyHouseMouse card = new CheekyHouseMouse();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventurePreventsPowerThreeBlockersButAllowsPowerTwoBlockers() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        CheekyHouseMouse card = new CheekyHouseMouse();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        Permanent highPowerBlocker = addCreatureReady(player2, new HillGiant());
        target.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(highPowerBlocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(target)))))
                .isInstanceOf(IllegalStateException.class);

        gd.playerBattlefields.get(player2.getId()).clear();
        Permanent lowPowerBlocker = addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(lowPowerBlocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(target))));

        assertThat(lowPowerBlocker.isBlocking()).isTrue();
    }

    @Test
    void adventureTargetMustBeCreatureYouControl() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        CheekyHouseMouse card = new CheekyHouseMouse();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control");
    }

    @Test
    void adventureRestrictionExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        CheekyHouseMouse card = new CheekyHouseMouse();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent blocker = addCreatureReady(player2, new HillGiant());
        target.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(target))));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
