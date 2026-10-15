package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.g.GristTheHungerTide;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrastaTempestsRoar.class, LightningBolt.class, GristTheHungerTide.class})
class ThrastaTempestsRoarTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {3} less for each other spell cast by any player this turn")
    void costReductionCountsSpellsCastByAnyPlayer() {
        harness.setHand(player1, List.of(new LightningBolt(), new ThrastaTempestsRoar()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());

        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, player1.getId());
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player1);
        resolveAllTriggers();

        harness.castCreature(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Has hexproof until the end of the turn it enters")
    void hexproofExpiresAtEndOfTurn() {
        harness.setHand(player1, List.of(new ThrastaTempestsRoar()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent thrasta = findPermanent(player1, "Thrasta, Tempest's Roar");
        assertThat(gqs.hasKeyword(gd, thrasta, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, thrasta, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void doesNotCountItselfForCostReduction() {
        harness.setHand(player1, List.of(new ThrastaTempestsRoar()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void fourSpellsReduceGenericCostToZeroButStillRequireGreenMana() {
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(),
                new LightningBolt(), new LightningBolt(), new ThrastaTempestsRoar()));
        harness.addMana(player1, ManaColor.RED, 4);
        for (int i = 0; i < 4; i++) {
            harness.castInstant(player1, 0, player2.getId());
        }
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void hexproofPreventsOpponentTargetingButAllowsControllerTargeting() {
        harness.setHand(player1, List.of(new ThrastaTempestsRoar(), new LightningBolt()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent thrasta = findPermanent(player1, "Thrasta, Tempest's Roar");
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, thrasta.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.castInstant(player1, 0, thrasta.getId());
        harness.passBothPriorities();
        assertThat(thrasta.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void hasHexproofWhenEnteringWithoutBeingCast() {
        Permanent thrasta = harness.enterBattlefieldAndReturn(player1, new ThrastaTempestsRoar());

        assertThat(gqs.hasKeyword(gd, thrasta, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void canAttackOnEntryAndTrampleOverPlaneswalker() {
        Permanent grist = harness.addToBattlefieldAndReturn(player2, new GristTheHungerTide());
        grist.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new ThrastaTempestsRoar()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, grist.getId()));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(grist);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
