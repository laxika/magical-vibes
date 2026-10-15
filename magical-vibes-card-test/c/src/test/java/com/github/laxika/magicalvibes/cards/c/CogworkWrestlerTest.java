package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BrackishBlunder;
import com.github.laxika.magicalvibes.cards.p.PlunderingPirate;
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

@CardUsed({CogworkWrestler.class, PlunderingPirate.class, BrackishBlunder.class})
class CogworkWrestlerTest extends BaseCardTest {

    @Test
    @DisplayName("Flash allows casting during an opponent's turn")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        gs.passPriority(gd, player2);
        harness.castFromHand(player1, new CogworkWrestler(), "{U}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("ETB gives an opponent's creature -2/-0 until end of turn")
    void weakensOpponentCreatureUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PlunderingPirate());
        UUID targetId = target.getId();

        harness.setHand(player1, List.of(new CogworkWrestler()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0, 0, targetId);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PlunderingPirate());
        UUID targetId = target.getId();
        harness.setHand(player1, List.of(new CogworkWrestler()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can resolve with no opposing creature to target")
    void resolvesWithoutLegalTarget() {
        harness.castFromHand(player1, new CogworkWrestler(), "{U}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Cogwork Wrestler");
        Permanent wrestler = findPermanent(player1, "Cogwork Wrestler");
        assertThat(gqs.getEffectivePower(gd, wrestler)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wrestler)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("ETB still resolves after Cogwork Wrestler leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CogworkWrestler());
        harness.setHand(player1, List.of(new CogworkWrestler()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        UUID sourceId = harness.getPermanentId(player1, "Cogwork Wrestler");
        harness.setHand(player1, List.of(new BrackishBlunder()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, sourceId);
        harness.assertNotOnBattlefield(player1, "Cogwork Wrestler");
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB does not affect a creature that leaves and returns before resolution")
    void returningTargetIsANewObject() {
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new CogworkWrestler());
        UUID targetId = harness.getPermanentId(player2, "Cogwork Wrestler");
        harness.setHand(player1, List.of(new CogworkWrestler()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new BrackishBlunder()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.assertInHand(player2, "Cogwork Wrestler");

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castCreature(player2, 0, harness.getPermanentId(player1, "Cogwork Wrestler"));
        resolveAllTriggers();

        Permanent returned = findPermanent(player2, "Cogwork Wrestler");
        assertThat(returned.getId()).isNotEqualTo(targetId);
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Cogwork Wrestler"))).isEqualTo(-1);
    }
}
