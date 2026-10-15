package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({DrownyardBehemoth.class, GrizzlyBears.class, Shock.class})
class DrownyardBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Emerge sacrifices a creature and reduces the generic cost by its mana value")
    void emergeSacrificesAndReducesCost() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new DrownyardBehemoth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(bearsId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drownyard Behemoth");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Hexproof lasts only while Drownyard Behemoth entered this turn")
    void hexproofExpiresAtEndOfTurn() {
        harness.setHand(player1, List.of(new DrownyardBehemoth()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent behemoth = findPermanent(player1, "Drownyard Behemoth");
        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.HEXPROOF)).isTrue();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, behemoth, Keyword.HEXPROOF)).isFalse();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, behemoth.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent cannot target Drownyard Behemoth while it has hexproof")
    void opponentCannotTargetWhileItHasHexproof() {
        harness.setHand(player1, List.of(new DrownyardBehemoth()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent behemoth = findPermanent(player1, "Drownyard Behemoth");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, behemoth.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    void canFlashInDuringOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new DrownyardBehemoth()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.castCreature(player1, 0);
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        harness.assertOnBattlefield(player1, "Drownyard Behemoth");
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Drownyard Behemoth"), Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void emergeCanBePaidAtInstantSpeedAndCannotReduceBlueMana() {
        harness.addToBattlefield(player1, new DrownyardBehemoth());
        UUID sacrificedId = harness.getPermanentId(player1, "Drownyard Behemoth");
        harness.setHand(player1, List.of(new DrownyardBehemoth()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrificedId)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Drownyard Behemoth");
        harness.assertNotInGraveyard(player1, "Drownyard Behemoth");

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrificedId));
        harness.assertInGraveyard(player1, "Drownyard Behemoth");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drownyard Behemoth");
        assertThat(harness.getPermanentId(player1, "Drownyard Behemoth")).isNotEqualTo(sacrificedId);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void controllerCanTargetItDuringItsEntryTurn() {
        harness.setHand(player1, List.of(new DrownyardBehemoth(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent behemoth = findPermanent(player1, "Drownyard Behemoth");
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, behemoth.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Drownyard Behemoth");
        assertThat(behemoth.getMarkedDamage()).isEqualTo(2);
    }
}
