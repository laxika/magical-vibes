package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MortalsResolve;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({XenagosGodOfRevels.class, GrizzlyBears.class, MortalsResolve.class})
class XenagosGodOfRevelsTest extends BaseCardTest {

    @Test
    @DisplayName("Xenagos is not a creature below seven combined red and green devotion")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent xenagos = addXenagos();
        addGreenDevotion(4);

        assertThat(gqs.isCreature(gd, xenagos)).isFalse();
        assertThat(gqs.isEnchantment(gd, xenagos)).isTrue();
    }

    @Test
    @DisplayName("Xenagos becomes a creature at seven combined red and green devotion")
    void becomesCreatureAtDevotionThreshold() {
        Permanent xenagos = addXenagos();
        addGreenDevotion(5);

        assertThat(gqs.isCreature(gd, xenagos)).isTrue();
    }

    @Test
    @DisplayName("Beginning of combat gives another creature +X/+X and haste, where X is its power")
    void beginningOfCombatBoostsAnotherCreatureByItsPower() {
        addXenagos();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The beginning-of-combat boost wears off at end of turn")
    void beginningOfCombatBoostWearsOffAtEndOfTurn() {
        addXenagos();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The ability targets only another creature controlled by Xenagos's controller")
    void targetsOnlyAnotherCreatureYouControl() {
        Permanent xenagos = addXenagos();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ownCreature.getId())
                .doesNotContain(xenagos.getId(), opponentCreature.getId());
    }

    @Test
    @DisplayName("The ability does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        addXenagos();
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The boost uses power at resolution and remains fixed afterward")
    void calculatesPowerAtResolution() {
        addXenagos();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MortalsResolve(), new MortalsResolve()));
        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(7);
    }

    @Test
    @DisplayName("Xenagos cannot target itself even when devotion makes it a creature")
    void cannotTargetItselfAsCreature() {
        Permanent xenagos = addXenagos();
        addGreenDevotion(5);
        assertThat(gqs.isCreature(gd, xenagos)).isTrue();

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).hasSize(5).doesNotContain(xenagos.getId());
    }

    @Test
    @DisplayName("Xenagos returns to being only an enchantment when devotion falls below seven")
    void losesCreatureTypeWhenDevotionDrops() {
        Permanent xenagos = addXenagos();
        addGreenDevotion(4);
        Permanent contributor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.isCreature(gd, xenagos)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(contributor);
        harness.runStateBasedActions();

        assertThat(gqs.isCreature(gd, xenagos)).isFalse();
        assertThat(gqs.isEnchantment(gd, xenagos)).isTrue();
    }

    @Test
    @DisplayName("There is no combat ability to resolve without another legal creature")
    void noOtherCreatureLeavesNoTriggerOnStack() {
        addXenagos();

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addXenagos() {
        return harness.addToBattlefieldAndReturn(player1, new XenagosGodOfRevels());
    }

    private void addGreenDevotion(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
