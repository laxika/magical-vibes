package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SauronTheLidlessEye.class, GrizzlyBears.class})
class SauronTheLidlessEyeTest extends BaseCardTest {

    @Test
    @DisplayName("Sauron's ETB steals, untaps, and grants haste to an opponent's creature")
    void etbStealsUntapsAndGrantsHaste() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SauronTheLidlessEye()));
        addSauronMana(player1);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(com.github.laxika.magicalvibes.model.Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("Sauron's activated ability boosts your creatures and makes each opponent lose life")
    void activatedAbilityBoostsCreaturesAndLosesLife() {
        Permanent sauron = addSauron(player1);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);
        addSauronMana(player1);

        int sauronIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sauron);
        harness.activateAbility(player1, sauronIndex, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Sauron's activated creature boost wears off at end of turn")
    void activatedAbilityWearsOffAtEndOfTurn() {
        Permanent sauron = addSauron(player1);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addSauronMana(player1);

        int sauronIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sauron);
        harness.activateAbility(player1, sauronIndex, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sauron's ETB cannot target a creature you control")
    void etbCannotTargetOwnCreature() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SauronTheLidlessEye()));
        addSauronMana(player1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    private Permanent addSauron(Player player) {
        Permanent sauron = harness.addToBattlefieldAndReturn(player, new SauronTheLidlessEye());
        sauron.setSummoningSick(false);
        return sauron;
    }

    private void addSauronMana(Player player) {
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }
}
