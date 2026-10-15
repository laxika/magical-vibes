package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BitterbladeWarrior.class})
class BitterbladeWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers the exert may prompt")
    void attackTriggersExertPrompt() {
        addCreatureReady(player1, new BitterbladeWarrior());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exerting gives +1/+0 and deathtouch until end of turn")
    void exertBoostsAndGrantsDeathtouch() {
        Permanent warrior = addCreatureReady(player1, new BitterbladeWarrior());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(gd.currentStep, this::resolveAllTriggers);

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Exerting keeps the creature tapped through its next untap step")
    void exertSkipsNextUntap() {
        Permanent warrior = addCreatureReady(player1, new BitterbladeWarrior());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(warrior.isTapped()).isTrue();
        assertThat(warrior.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Declining exert leaves base stats and grants no deathtouch")
    void decliningExertDoesNothing() {
        Permanent warrior = addCreatureReady(player1, new BitterbladeWarrior());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.DEATHTOUCH)).isFalse();
        assertThat(warrior.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Exert applies the untap restriction before its bonus trigger resolves")
    void exertRestrictionAppliesBeforeBonusResolves() {
        Permanent warrior = addCreatureReady(player1, new BitterbladeWarrior());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(gd.currentStep,
                () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.DEATHTOUCH)).isFalse();
        assertThat(warrior.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Exert bonuses expire at end of turn")
    void exertBonusesExpireAtEndOfTurn() {
        Permanent warrior = addCreatureReady(player1, new BitterbladeWarrior());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.DEATHTOUCH)).isFalse();
        assertThat(warrior.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Exert skips only the controller's next untap step")
    void exertSkipsExactlyOneControllerUntap() {
        Permanent warrior = addCreatureReady(player1, new BitterbladeWarrior());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.performUntapStep(player2);
        assertThat(warrior.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(warrior.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(warrior.isTapped()).isFalse();
    }
}
