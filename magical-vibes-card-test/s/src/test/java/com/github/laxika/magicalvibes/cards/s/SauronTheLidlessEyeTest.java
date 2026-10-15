package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
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

    @Test
    @DisplayName("The stolen creature returns and loses haste at end of turn")
    void stolenCreatureReturnsAndLosesHaste() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SauronTheLidlessEye()));
        addSauronMana(player1);
        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Repeated activations boost Sauron and stack, without boosting later creatures")
    void activationsStackAndOnlyBoostCreaturesPresentAtResolution() {
        Permanent sauron = harness.addToBattlefieldAndReturn(player1, new SauronTheLidlessEye());
        sauron.tap();
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent laterCreature = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, sauron)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
        assertThat(sauron.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An activated ability still makes opponents lose life after Sauron leaves")
    void activatedAbilityResolvesWithoutSourceOrCreatures() {
        Permanent sauron = addSauron(player1);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, sauron);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 18);
    }

    private Permanent addSauron(Player player) {
        return addCreatureReady(player, new SauronTheLidlessEye());
    }

    private void addSauronMana(Player player) {
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }
}
