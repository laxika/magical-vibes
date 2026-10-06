package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({EmberhornMinotaur.class})
class EmberhornMinotaurTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers the exert may prompt")
    void attackTriggersExertPrompt() {
        addReadyMinotaur(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exerting gives +1/+1 and menace until end of turn")
    void exertBoostsAndGrantsMenace() {
        Permanent minotaur = addReadyMinotaur(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, minotaur)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, minotaur)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Exerting keeps the creature tapped through its next untap step")
    void exertSkipsNextUntap() {
        Permanent minotaur = addReadyMinotaur(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(minotaur.isTapped()).isTrue();
        assertThat(minotaur.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Declining exert leaves base stats and grants no menace")
    void decliningExertDoesNothing() {
        Permanent minotaur = addReadyMinotaur(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, minotaur)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, minotaur)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.MENACE)).isFalse();
        assertThat(minotaur.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Exert is chosen during attacker declaration before priority")
    void exertChoicePrecedesPriority() {
        addReadyMinotaur(player1);

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Exert pays the untap restriction before the bonus trigger resolves")
    void exertCostIsPaidBeforeBonusResolves() {
        Permanent minotaur = addReadyMinotaur(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(minotaur.getSkipUntapCount()).isGreaterThan(0);
        assertThat(gqs.getEffectivePower(gd, minotaur)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, minotaur)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.MENACE)).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, minotaur)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Exert skips only the next untap step of the exerting player")
    void exertExpiresAfterOneUntapStep() {
        Permanent minotaur = addReadyMinotaur(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, minotaur)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, minotaur)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.MENACE)).isFalse();
        harness.performUntapStep(player1);
        assertThat(minotaur.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(minotaur.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An exerted creature can untap during a new controller's untap step")
    void exertDoesNotRestrictNewControllersUntap() {
        Permanent minotaur = addReadyMinotaur(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(minotaur);
        gd.playerBattlefields.get(player2.getId()).add(minotaur);
        harness.performUntapStep(player2);

        assertThat(minotaur.isTapped()).isFalse();
    }

    private Permanent addReadyMinotaur(Player player) {
        return addCreatureReady(player, new EmberhornMinotaur());
    }
}
